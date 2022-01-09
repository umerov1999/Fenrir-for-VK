package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.dummy;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.RxUtils.subscribeOnIOAndIgnore;
import static dev.ragnarok.fenrir.util.Utils.copyToArrayListWithPredicate;
import static dev.ragnarok.fenrir.util.Utils.findInfoByPredicate;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.Attachments;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.WallEditorAttrs;
import dev.ragnarok.fenrir.mvp.view.IPostCreateView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.MessageMethod;
import dev.ragnarok.fenrir.upload.Method;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Predicate;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PostCreatePresenter extends AbsPostEditPresenter<IPostCreateView> {

    private final int ownerId;

    @EditingPostType
    private final int editingType;
    private final WallEditorAttrs attrs;
    private final IAttachmentsRepository attachmentsRepository;
    private final IWallsRepository walls;
    private final String mime;
    private Post post;
    private boolean postPublished;
    private Optional<ArrayList<Uri>> upload;
    private boolean publishingNow;
    private String links;

    public PostCreatePresenter(int accountId, int ownerId, @EditingPostType int editingType,
                               ModelsBundle bundle, @NonNull WallEditorAttrs attrs, @Nullable ArrayList<Uri> streams, @Nullable String links, @Nullable String mime, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        upload = Optional.wrap(streams);
        this.mime = mime;
        attachmentsRepository = Injection.provideAttachmentsRepository();
        walls = Repository.INSTANCE.getWalls();

        this.attrs = attrs;
        this.ownerId = ownerId;
        this.editingType = editingType;

        if (!isEmpty(links))
            this.links = links;

        if (isNull(savedInstanceState) && nonNull(bundle)) {
            for (AbsModel i : bundle) {
                getData().add(new AttachmentEntry(false, i));
            }
        }

        setupAttachmentsListening();
        setupUploadListening();

        restoreEditingWallPostFromDbAsync();

        // только на моей стене
        setFriendsOnlyOptionAvailable(ownerId > 0 && ownerId == accountId);

        // доступно только в группах и только для редакторов и выше
        setFromGroupOptionAvailable(isGroup() && isEditorOrHigher());

        // доступно только для публичных страниц(и я одмен) или если нажат "От имени группы"
        setAddSignatureOptionAvailable((isCommunity() && isEditorOrHigher()) || fromGroup.get());
    }

    @Override
    public void onGuiCreated(@NonNull IPostCreateView view) {
        super.onGuiCreated(view);

        @StringRes
        int toolbarTitleRes = isCommunity() && !isEditorOrHigher() ? R.string.title_suggest_news : R.string.title_activity_create_post;
        view.setToolbarTitle(getString(toolbarTitleRes));
        view.setToolbarSubtitle(getOwner().getFullName());

        resolveSignerInfo();
        resolveSupportButtons();
        resolvePublishDialogVisibility();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        checkUploadUris();
    }

    private void checkUploadUris() {
        if (post != null && upload.nonEmpty()) {
            List<Uri> uris = upload.get();

            Integer size = Settings.get()
                    .main()
                    .getUploadImageSize();

            boolean isVideo = ActivityUtils.isMimeVideo(mime);

            if (isNull(size) && !isVideo) {
                callResumedView(v -> v.displayUploadUriSizeDialog(uris));
            } else {
                uploadStreamsImpl(uris, isNull(size) ? 0 : size, isVideo);
            }
        }
    }

    private void uploadStreamsImpl(@NonNull List<Uri> streams, int size, boolean isVideo) {
        AssertUtils.requireNonNull(post);

        upload = Optional.empty();

        UploadDestination destination = isVideo ? UploadDestination.forPost(post.getDbid(), ownerId, MessageMethod.VIDEO) : UploadDestination.forPost(post.getDbid(), ownerId);
        List<UploadIntent> intents = new ArrayList<>(streams.size());

        if (!isVideo) {
            for (Uri uri : streams) {
                intents.add(new UploadIntent(getAccountId(), destination)
                        .setAutoCommit(true)
                        .setFileUri(uri)
                        .setSize(size));
            }
        } else {
            for (Uri uri : streams) {
                intents.add(new UploadIntent(getAccountId(), destination)
                        .setAutoCommit(true)
                        .setFileUri(uri));
            }
        }

        uploadManager.enqueue(intents);
    }

    @Override
    void onFromGroupChecked(boolean checked) {
        super.onFromGroupChecked(checked);

        setAddSignatureOptionAvailable(checked);
        resolveSignerInfo();
    }

    @Override
    void onShowAuthorChecked(boolean checked) {
        resolveSignerInfo();
    }

    private void resolveSignerInfo() {
        boolean visible = false;

        if (isGroup() && !isEditorOrHigher() || !fromGroup.get() || addSignature.get()) {
            visible = true;
        }

        if (isCommunity() && isEditorOrHigher()) {
            visible = addSignature.get();
        }

        Owner author = getAuthor();

        boolean finalVisible = visible;
        callView(v -> {
            v.displaySignerInfo(author.getFullName(), author.get100photoOrSmaller());
            v.setSignerInfoVisible(finalVisible);
        });
    }

    private Owner getAuthor() {
        return attrs.getEditor();
    }

    private boolean isEditorOrHigher() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.EDITOR;
    }

    private boolean isGroup() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getType() == VKApiCommunity.Type.GROUP;
    }

    private boolean isCommunity() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getType() == VKApiCommunity.Type.PAGE;
    }

    @Override
    ArrayList<AttachmentEntry> getNeedParcelSavingEntries() {
        Predicate<AttachmentEntry> predicate = entry -> {
            // сохраняем только те, что не лежат в базе
            AbsModel model = entry.getAttachment();
            return !(model instanceof Upload) && entry.getOptionalId() == 0;
        };

        return copyToArrayListWithPredicate(getData(), predicate);
    }

    private void setupAttachmentsListening() {
        appendDisposable(attachmentsRepository.observeAdding()
                .filter(this::filterAttachmentEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onRepositoryAttachmentsAdded(event.getAttachments())));

        appendDisposable(attachmentsRepository.observeRemoving()
                .filter(this::filterAttachmentEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onRepositoryAttachmentsRemoved));
    }

    private void setupUploadListening() {
        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadObjectRemovedFromQueue));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, this::isUploadToThis)));
    }

    private boolean isUploadToThis(Upload upload) {
        UploadDestination dest = upload.getDestination();
        return nonNull(post)
                && dest.getMethod() == Method.TO_WALL
                && dest.getOwnerId() == ownerId
                && dest.getId() == post.getDbid();
    }

    private void restoreEditingWallPostFromDbAsync() {
        appendDisposable(walls
                .getEditingPost(getAccountId(), ownerId, editingType, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPostRestored, Analytics::logUnexpectedError));
    }

    private void restoreEditingAttachmentsAsync(int postDbid) {
        appendDisposable(attachmentsSingle(postDbid)
                .zipWith(uploadsSingle(postDbid), this::combine)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAttachmentsRestored, Analytics::logUnexpectedError));
    }

    private void onPostRestored(Post post) {
        this.post = post;
        checkFriendsOnly(post.isFriendsOnly());

        boolean postpone = post.getPostType() == VKApiPost.Type.POSTPONE;
        setTimerValue(postpone ? post.getDate() : null);

        setTextBody(post.getText());

        if (!isEmpty(links)) {
            setTextBody(links);
            links = null;
        }

        restoreEditingAttachmentsAsync(post.getDbid());
    }

    private Single<List<AttachmentEntry>> attachmentsSingle(int postDbid) {
        return attachmentsRepository
                .getAttachmentsWithIds(getAccountId(), AttachToType.POST, postDbid)
                .map(pairs -> createFrom(pairs, true));
    }

    private Single<List<AttachmentEntry>> uploadsSingle(int postDbid) {
        UploadDestination destination = UploadDestination.forPost(postDbid, ownerId);
        return uploadManager
                .get(getAccountId(), destination)
                .flatMap(u -> Single.just(createFrom(u)));
    }

    private void onAttachmentsRestored(List<AttachmentEntry> data) {
        if (nonEmpty(data)) {
            int size = getData().size();

            getData().addAll(data);

            safelyNotifyItemsAdded(size, data.size());
        }

        checkUploadUris();
    }

    private void onRepositoryAttachmentsRemoved(IAttachmentsRepository.IRemoveEvent event) {
        Pair<Integer, AttachmentEntry> info = findInfoByPredicate(getData(), entry -> entry.getOptionalId() == event.getGeneratedId());

        if (nonNull(info)) {
            AttachmentEntry entry = info.getSecond();
            int index = info.getFirst();

            getData().remove(index);
            safelyNotifyItemRemoved(index);

            if (entry.getAttachment() instanceof Poll) {
                resolveSupportButtons();
            }
        }
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        if (isNull(post)) {
            return;
        }

        UploadDestination destination = UploadDestination.forPost(post.getDbid(), ownerId);
        uploadManager.enqueue(UploadUtils.createIntents(getAccountId(), destination, photos, size, true));
    }

    @Override
    protected void doUploadFile(String file, int size) {
        if (isNull(post)) {
            return;
        }

        UploadDestination destination = UploadDestination.forPost(post.getDbid(), ownerId);
        uploadManager.enqueue(UploadUtils.createIntents(getAccountId(), destination, file, size, true));
    }

    private boolean filterAttachmentEvents(IAttachmentsRepository.IBaseEvent event) {
        return nonNull(post)
                && event.getAttachToType() == AttachToType.POST
                && event.getAccountId() == getAccountId()
                && event.getAttachToId() == post.getDbid();
    }

    private void onRepositoryAttachmentsAdded(List<Pair<Integer, AbsModel>> data) {
        boolean pollAdded = false;

        int size = getData().size();

        for (Pair<Integer, AbsModel> pair : data) {
            AbsModel model = pair.getSecond();
            if (model instanceof Poll) {
                pollAdded = true;
            }

            getData().add(new AttachmentEntry(true, model).setOptionalId(pair.getFirst()));
        }

        safelyNotifyItemsAdded(size, data.size());

        if (pollAdded) {
            resolveSupportButtons();
        }
    }

    @Override
    protected void onPollCreateClick() {
        callView(v -> v.openPollCreationWindow(getAccountId(), ownerId));
    }

    @Override
    protected void onModelsAdded(List<? extends AbsModel> models) {
        appendDisposable(attachmentsRepository.attach(getAccountId(), AttachToType.POST, post.getDbid(), models)
                .subscribeOn(Schedulers.io())
                .subscribe(dummy(), ignore()));
    }

    @Override
    protected void onAttachmentRemoveClick(int index, @NonNull AttachmentEntry attachment) {
        if (attachment.getOptionalId() != 0) {
            appendDisposable(attachmentsRepository.remove(getAccountId(), AttachToType.POST, post.getDbid(), attachment.getOptionalId())
                    .subscribeOn(Schedulers.io())
                    .subscribe(dummy(), ignore()));
        } else {
            manuallyRemoveElement(index);
        }
    }

    @Override
    protected void onTimerClick() {
        if (post.getPostType() == VKApiPost.Type.POSTPONE) {
            post.setPostType(VKApiPost.Type.POST);
            setTimerValue(null);
            resolveTimerInfoView();
            return;
        }

        long initialTime = post.getDate() == 0 ? System.currentTimeMillis() / 1000 + 2 * 60 * 60 : post.getDate();
        callView(v -> v.showEnterTimeDialog(initialTime));
    }

    public void fireTimerTimeSelected(long unixtime) {
        post.setPostType(VKApiPost.Type.POSTPONE);
        post.setDate(unixtime);

        setTimerValue(unixtime);
    }

    private void resolveSupportButtons() {
        callView(v -> v.setSupportedButtons(true, true, true, true, isPollSupported(), isSupportTimer()));
    }

    private boolean isPollSupported() {
        for (AttachmentEntry entry : getData()) {
            if (entry.getAttachment() instanceof Poll) {
                return false;
            }
        }

        return true;
    }

    private Owner getOwner() {
        return attrs.getOwner();
    }

    private boolean isSupportTimer() {
        if (ownerId > 0) {
            return getAccountId() == ownerId;
        } else {
            return isEditorOrHigher();
        }
    }

    private void changePublishingNowState(boolean publishing) {
        publishingNow = publishing;
        resolvePublishDialogVisibility();
    }

    private void resolvePublishDialogVisibility() {
        if (publishingNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.publication, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    private void commitDataToPost() {
        if (isNull(post.getAttachments())) {
            post.setAttachments(new Attachments());
        }

        for (AttachmentEntry entry : getData()) {
            post.getAttachments().add(entry.getAttachment());
        }

        post.setText(getTextBody());
        post.setFriendsOnly(friendsOnly.get());
    }

    public void fireReadyClick() {
        UploadDestination destination = UploadDestination.forPost(post.getDbid(), ownerId);
        appendDisposable(uploadManager.get(getAccountId(), destination)
                .map(List::size)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(count -> {
                    if (count > 0) {
                        callView(v -> v.showError(R.string.wait_until_file_upload_is_complete));
                    } else {
                        doPost();
                    }
                }, Analytics::logUnexpectedError));
    }

    private void doPost() {
        commitDataToPost();

        changePublishingNowState(true);

        boolean fromGroup = super.fromGroup.get();
        boolean showSigner = addSignature.get();
        int accountId = getAccountId();

        appendDisposable(walls
                .post(accountId, post, fromGroup, showSigner)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPostPublishSuccess, this::onPostPublishError));
    }

    @SuppressWarnings("unused")
    private void onPostPublishSuccess(Post post) {
        changePublishingNowState(false);
        releasePostDataAsync();

        postPublished = true;

        callView(IPostCreateView::goBack);
    }

    private void onPostPublishError(Throwable t) {
        changePublishingNowState(false);
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void releasePostDataAsync() {
        if (isNull(post)) {
            return;
        }

        UploadDestination destination = UploadDestination.forPost(post.getDbid(), ownerId);
        uploadManager.cancelAll(getAccountId(), destination);

        subscribeOnIOAndIgnore(walls.deleteFromCache(getAccountId(), post.getDbid()));
    }

    private void safeDraftAsync() {
        commitDataToPost();

        int accountId = getAccountId();
        subscribeOnIOAndIgnore(walls.cachePostWithIdSaving(accountId, post));
    }

    public boolean onBackPresed() {
        if (postPublished) {
            return true;
        }

        if (EditingPostType.TEMP == editingType) {
            releasePostDataAsync();
        } else {
            safeDraftAsync();
        }

        return true;
    }

    public void fireUriUploadSizeSelected(List<Uri> uris, int size) {
        uploadStreamsImpl(uris, size, false);
    }

    public void fireUriUploadCancelClick() {
        upload = Optional.empty();
    }
}
