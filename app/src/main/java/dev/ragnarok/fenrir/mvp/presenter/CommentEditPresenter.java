package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.ICommentsInteractor;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.impl.CommentsInteractor;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.view.ICommentEditView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Predicate;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class CommentEditPresenter extends AbsAttachmentsEditPresenter<ICommentEditView> {

    private final Comment orig;
    private final UploadDestination destination;
    private final ICommentsInteractor commentsInteractor;
    private final Integer CommentThread;
    private boolean editingNow;
    private boolean canGoBack;

    public CommentEditPresenter(Comment comment, int accountId, Integer CommentThread, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        commentsInteractor = new CommentsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
        orig = comment;
        destination = UploadDestination.forComment(comment.getId(), comment.getCommented().getSourceOwnerId());
        this.CommentThread = CommentThread;

        if (isNull(savedInstanceState)) {
            setTextBody(orig.getText());
            initialPopulateEntries();
        }

        appendDisposable(uploadManager.get(getAccountId(), destination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUploadsReceived));

        Predicate<Upload> predicate = upload -> upload.getAccountId() == getAccountId() && destination.compareTo(upload.getDestination());

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(added -> onUploadQueueUpdates(added, predicate)));

        appendDisposable(uploadManager.observeDeleting(false)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadObjectRemovedFromQueue));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadManager.observeResults()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsQueueChanged));
    }

    private void onUploadsQueueChanged(Pair<Upload, UploadResult<?>> pair) {
        Upload upload = pair.getFirst();
        UploadResult<?> result = pair.getSecond();

        int index = findUploadIndexById(upload.getId());

        AttachmentEntry entry;
        if (result.getResult() instanceof Photo) {
            entry = new AttachmentEntry(true, (Photo) result.getResult());
        } else {
            // not supported!!!
            return;
        }

        if (index != -1) {
            getData().set(index, entry);
        } else {
            getData().add(0, entry);
        }

        safeNotifyDataSetChanged();
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmentEntry attachment) {
        manuallyRemoveElement(index);
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, size, false);
        uploadManager.enqueue(intents);
    }

    @Override
    protected void doUploadFile(String file, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, file, size, false);
        uploadManager.enqueue(intents);
    }

    private void onUploadsReceived(List<Upload> uploads) {
        getData().addAll(createFrom(uploads));
        safeNotifyDataSetChanged();
    }

    @Override
    ArrayList<AttachmentEntry> getNeedParcelSavingEntries() {
        // сохраняем все, кроме аплоада
        return Utils.copyToArrayListWithPredicate(getData(), entry -> !(entry.getAttachment() instanceof Upload));
    }

    @Override
    public void onGuiCreated(@NonNull ICommentEditView view) {
        super.onGuiCreated(view);

        resolveButtonsAvailability();
        resolveProgressDialog();
    }

    private void resolveButtonsAvailability() {
        callView(v -> v.setSupportedButtons(true, true, true, true, false, false));
    }

    private void initialPopulateEntries() {
        if (nonNull(orig.getAttachments())) {
            List<AbsModel> models = orig.getAttachments().toList();

            for (AbsModel m : models) {
                getData().add(new AttachmentEntry(true, m));
            }
        }
    }

    public void fireReadyClick() {
        if (hasUploads()) {
            callView(v -> v.showError(R.string.upload_not_resolved_exception_message));
            return;
        }

        List<AbsModel> models = new ArrayList<>();
        for (AttachmentEntry entry : getData()) {
            models.add(entry.getAttachment());
        }

        setEditingNow(true);

        int accountId = getAccountId();
        Commented commented = orig.getCommented();
        int commentId = orig.getId();
        String body = getTextBody();

        appendDisposable(commentsInteractor.edit(accountId, commented, commentId, body, CommentThread, models)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onEditComplete, this::onEditError));
    }

    private void onEditError(Throwable t) {
        setEditingNow(false);
        callView(v -> showError(v, t));
    }

    private void onEditComplete(@Nullable Comment comment) {
        setEditingNow(false);

        canGoBack = true;

        callView(view -> view.goBackWithResult(comment));
    }

    private void setEditingNow(boolean editingNow) {
        this.editingNow = editingNow;
        resolveProgressDialog();
    }

    private void resolveProgressDialog() {
        if (editingNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.saving, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    public boolean onBackPressed() {
        if (canGoBack) {
            return true;
        }

        callView(ICommentEditView::showConfirmWithoutSavingDialog);
        return false;
    }

    public void fireSavingCancelClick() {
        uploadManager.cancelAll(getAccountId(), destination);
        canGoBack = true;
        callView(ICommentEditView::goBack);
    }
}