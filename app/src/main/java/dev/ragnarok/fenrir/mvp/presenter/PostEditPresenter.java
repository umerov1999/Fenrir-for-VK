package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.applyCompletableIOToMainSchedulers;
import static dev.ragnarok.fenrir.util.Utils.copyToArrayListWithPredicate;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Includes;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.WallEditorAttrs;
import dev.ragnarok.fenrir.mvp.view.IBaseAttachmentsEditView;
import dev.ragnarok.fenrir.mvp.view.IPostEditView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Predicate;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Unixtime;
import dev.ragnarok.fenrir.util.Utils;


public class PostEditPresenter extends AbsPostEditPresenter<IPostEditView> {

    private static final String TAG = PostEditPresenter.class.getSimpleName();

    private static final String SAVE_POST = "save_post";

    private final Post post;
    private final UploadDestination uploadDestination;
    private final Predicate<Upload> uploadPredicate;

    private final IWallsRepository wallInteractor;

    private final WallEditorAttrs attrs;

    private boolean editingNow;
    private boolean canExit;

    public PostEditPresenter(int accountId, @NonNull Post post, @NonNull WallEditorAttrs attrs, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        wallInteractor = Repository.INSTANCE.getWalls();
        this.attrs = attrs;

        if (isNull(savedInstanceState)) {
            this.post = safelyClone(post);

            setTextBody(post.getText());

            if (post.getPostType() == VKApiPost.Type.POSTPONE) {
                setTimerValue(post.getDate());
            }

            if (nonNull(post.getAttachments())) {
                List<AbsModel> list = post.getAttachments().toList();

                for (AbsModel model : list) {
                    getData().add(new AttachmentEntry(true, model));
                }
            }

            if (post.hasCopyHierarchy()) {
                getData().add(0, new AttachmentEntry(false, post.getCopyHierarchy().get(0)));
            }
        } else {
            this.post = savedInstanceState.getParcelable(SAVE_POST);
        }

        Owner owner = getOwner();

        setFriendsOnlyOptionAvailable(owner.getOwnerId() > 0 && owner.getOwnerId() == accountId);
        checkFriendsOnly(post.isFriendsOnly());

        setAddSignatureOptionAvailable(canAddSignature());

        addSignature.setValue(post.getSignerId() > 0);
        setFromGroupOptionAvailable(false); // only for publishing

        uploadDestination = UploadDestination.forPost(post.getVkid(), post.getOwnerId());
        uploadPredicate = object -> object.getAccountId() == getAccountId()
                && object.getDestination().compareTo(uploadDestination);

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Includes.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, uploadPredicate)));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Includes.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Includes.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeDeleting(false)
                .observeOn(Includes.provideMainThreadScheduler())
                .subscribe(this::onUploadObjectRemovedFromQueue));

        appendDisposable(uploadManager.observeResults()
                .observeOn(Includes.provideMainThreadScheduler())
                .subscribe(this::onUploadComplete));
    }

    private static Post safelyClone(Post post) {
        try {
            return post.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Unable to clone post");
        }
    }

    private static boolean validatePublishDate(long unixtime) {
        return Unixtime.now() < unixtime;
    }

    @NonNull
    private Owner getOwner() {
        return attrs.getOwner();
    }

    private Owner getMe() {
        return attrs.getEditor();
    }

    @Override
    void onShowAuthorChecked(boolean checked) {
        super.onShowAuthorChecked(checked);
        resolveSignerInfoVisibility();
    }

    private boolean isEditorOrHigher() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.EDITOR;
    }

    private boolean postIsSuggest() {
        return post.getPostType() == VKApiPost.Type.SUGGEST;
    }

    private boolean postIsMine() {
        if (post.getCreatorId() > 0 && post.getCreatorId() == getAccountId()) {
            return true;
        }

        return post.getSignerId() > 0 && post.getSignerId() == getAccountId();

    }

    private boolean supportSignerInfoDisplaying() {
        if (!isAddSignatureOptionAvailable()) {
            return false;
        }

        // потому что она может быть недоступна (signer == null)
        if (postIsSuggest() && !postIsMine()) {
            return true;
        }

        return nonNull(post.getCreator());
    }

    @Override
    public void onGuiCreated(@NonNull IPostEditView view) {
        super.onGuiCreated(view);

        @StringRes
        int titleRes = isPublishingSuggestPost() ? R.string.publication : R.string.editing;

        view.setToolbarTitle(getString(titleRes));
        view.setToolbarSubtitle(getOwner().getFullName());

        resolveSignerInfoVisibility();
        resolveProgressDialog();
        resolveSupportButtons();
    }

    private Owner getDisplayedSigner() {
        if (postIsSuggest()) {
            return post.getAuthor();
        }

        if (nonNull(post.getCreator())) {
            return post.getCreator();
        }

        return getMe();
    }

    private void resolveSignerInfoVisibility() {
        Owner signer = getDisplayedSigner();

        callView(v -> {
            v.displaySignerInfo(signer.getFullName(), signer.get100photoOrSmaller());
            v.setSignerInfoVisible(supportSignerInfoDisplaying() && addSignature.get());
        });
    }

    @Override
    ArrayList<AttachmentEntry> getNeedParcelSavingEntries() {
        return copyToArrayListWithPredicate(getData(), entry -> !(entry.getAttachment() instanceof Upload));
    }

    private void onUploadComplete(Pair<Upload, UploadResult<?>> data) {
        Upload upload = data.getFirst();
        UploadResult<?> result = data.getSecond();

        int index = findUploadIndexById(upload.getId());
        if (index == -1) {
            return;
        }

        if (result.getResult() instanceof Photo) {
            Photo photo = (Photo) result.getResult();
            getData().set(index, new AttachmentEntry(true, photo));
        } else {
            getData().remove(index);
        }

        callView(IBaseAttachmentsEditView::notifyDataSetChanged);
    }

    private void setEditingNow(boolean editingNow) {
        this.editingNow = editingNow;
        resolveProgressDialog();
    }

    private void resolveProgressDialog() {
        if (editingNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.publication, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    private boolean isPublishingSuggestPost() {
        // если пост предложенный - постим, если нет - редактируем
        // если пост мой и он предложенный - редактируем
        return postIsSuggest() && !postIsMine();
    }

    private void save() {
        Logger.d(TAG, "save, author: " + post.getAuthor() + ", signer: " + post.getCreator());

        appendDisposable(uploadManager
                .get(getAccountId(), uploadDestination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    if (data.isEmpty()) {
                        doCommitImpl();
                    } else {
                        callView(v -> v.showToast(R.string.wait_until_file_upload_is_complete, true));
                    }
                }, Analytics::logUnexpectedError));
    }

    private boolean isGroup() {
        return getOwner() instanceof Community && ((Community) getOwner()).getType() == VKApiCommunity.Type.GROUP;
    }

    private boolean isCommunity() {
        return getOwner() instanceof Community && ((Community) getOwner()).getType() == VKApiCommunity.Type.PAGE;
    }

    private boolean canAddSignature() {
        if (!isEditorOrHigher()) {
            // только редакторы и выше могу указывать автора
            return false;
        }

        if (isGroup()) {
            // если группа - то только, если пост от имени группы
            return post.getAuthor() instanceof Community;
        }

        // в публичных страницах всегда можно
        return isCommunity();

    }

    private void doCommitImpl() {
        if (isPublishingSuggestPost()) {
            postImpl();
            return;
        }

        boolean timerCancelled = post.getPostType() == VKApiPost.Type.POSTPONE && isNull(getTimerValue());

        if (timerCancelled) {
            postImpl();
            return;
        }

        saveImpl();
    }

    private void saveImpl() {
        Long publishDate = getTimerValue();

        setEditingNow(true);
        Boolean signed = canAddSignature() ? addSignature.get() : null;
        Boolean friendsOnly = isFriendsOnlyOptionAvailable() ? super.friendsOnly.get() : null;

        appendDisposable(wallInteractor
                .editPost(getAccountId(), post.getOwnerId(), post.getVkid(), friendsOnly,
                        getTextBody(), getAttachmentTokens(), null, signed, publishDate,
                        null, null, null, null)
                .compose(applyCompletableIOToMainSchedulers())
                .subscribe(this::onEditResponse, throwable -> onEditError(getCauseIfRuntime(throwable))));
    }

    private void postImpl() {
        int accountId = getAccountId();
        Long publishDate = getTimerValue();
        String body = getTextBody();
        Boolean signed = isAddSignatureOptionAvailable() ? addSignature.get() : null;

        // Эта опция не может быть доступна (так как публикация - исключительно для PAGE)
        Boolean fromGroup = null;

        setEditingNow(true);
        appendDisposable(wallInteractor
                .post(accountId, post.getOwnerId(), null, fromGroup, body, getAttachmentTokens(), null,
                        signed, publishDate, null, null, null, post.getVkid(), null, null, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(post -> onEditResponse(), throwable -> onEditError(getCauseIfRuntime(throwable))));
    }

    private void onEditError(Throwable throwable) {
        setEditingNow(false);
        throwable.printStackTrace();

        callView(v -> showError(v, throwable));
    }

    private void onEditResponse() {
        setEditingNow(false);
        canExit = true;
        callView(IPostEditView::closeAsSuccess);
    }

    private List<AbsModel> getAttachmentTokens() {
        List<AbsModel> result = new ArrayList<>();

        for (AttachmentEntry entry : getData()) {
            if (entry.getAttachment() instanceof Post) {
                continue;
            }

            result.add(entry.getAttachment());
        }

        return result;
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmentEntry attachment) {
        manuallyRemoveElement(index);

        if (attachment.getAttachment() instanceof Poll) {
            // because only 1 poll is supported
            resolveSupportButtons();
        }
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), uploadDestination, photos, size, false);
        uploadManager.enqueue(intents);
    }

    @Override
    protected void doUploadFile(String file, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), uploadDestination, file, size, false);
        uploadManager.enqueue(intents);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_POST, post);
    }

    private void resolveSupportButtons() {
        if (post.hasCopyHierarchy()) {
            callView(v -> v.setSupportedButtons(false, false, false, false, false, false));
        } else {
            callView(v -> v.setSupportedButtons(true, true, true, true, isPollSupported(), supportTimer()));
        }
    }

    private boolean isPollSupported() {
        for (AttachmentEntry entry : getData()) {
            if (entry.getAttachment() instanceof Poll) {
                return false;
            }
        }

        return true;
    }

    private boolean supportTimer() {
        if (getOwner() instanceof Community && ((Community) getOwner()).getAdminLevel() < VKApiCommunity.AdminLevel.EDITOR) {
            // если сообщество и я не одмен, то нет
            return false;
        }

        return Utils.intValueIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST);
    }

    public final void fireReadyClick() {
        save();
    }

    public boolean onBackPressed() {
        if (canExit) {
            return true;
        }

        callView(IPostEditView::showConfirmExitDialog);
        return false;
    }

    @Override
    protected void onTimerClick() {
        if (!supportTimer()) {
            return;
        }

        if (nonNull(getTimerValue())) {
            setTimerValue(null);
            return;
        }

        long initialDate = Unixtime.now() + 24 * 60 * 60;

        callView(v -> v.showEnterTimeDialog(initialDate));
    }

    @Override
    public void fireTimerTimeSelected(long unixtime) {
        if (!validatePublishDate(unixtime)) {
            callView(v -> v.showError(R.string.date_is_invalid));
            return;
        }

        setTimerValue(unixtime);
    }

    @Override
    protected void onPollCreateClick() {
        callView(v -> v.openPollCreationWindow(getAccountId(), post.getOwnerId()));
    }

    public void fireExitWithSavingConfirmed() {
        save();
    }

    public void fireExitWithoutSavingClick() {
        canExit = true;
        uploadManager.cancelAll(getAccountId(), uploadDestination);
        callView(IPostEditView::closeAsSuccess);
    }
}