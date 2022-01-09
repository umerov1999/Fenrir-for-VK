package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.RxUtils.subscribeOnIOAndIgnore;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.removeIf;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.mvp.view.ICreateCommentView;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Predicate;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Single;


public class CommentCreatePresenter extends AbsAttachmentsEditPresenter<ICreateCommentView> {

    private final int commentId;
    private final UploadDestination destination;
    private final IAttachmentsRepository attachmentsRepository;

    public CommentCreatePresenter(int accountId, int commentDbid, int sourceOwnerId, String body, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        attachmentsRepository = Injection.provideAttachmentsRepository();
        commentId = commentDbid;
        destination = UploadDestination.forComment(commentId, sourceOwnerId);

        if (isNull(savedInstanceState)) {
            setTextBody(body);
        }

        Predicate<Upload> predicate = o -> destination.compareTo(o.getDestination());

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, predicate)));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadObjectRemovedFromQueue));

        appendDisposable(attachmentsRepository.observeAdding()
                .filter(this::filterAttachEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::handleAttachmentsAdding));

        appendDisposable(attachmentsRepository.observeRemoving()
                .filter(this::filterAttachEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::handleAttachmentRemoving));

        loadAttachments();
    }

    private boolean filterAttachEvents(IAttachmentsRepository.IBaseEvent event) {
        return event.getAccountId() == getAccountId()
                && event.getAttachToId() == commentId
                && event.getAttachToType() == AttachToType.COMMENT;
    }

    private void handleAttachmentRemoving(IAttachmentsRepository.IRemoveEvent event) {
        if (removeIf(getData(), attachment -> attachment.getOptionalId() == event.getGeneratedId())) {
            safeNotifyDataSetChanged();
        }
    }

    private void handleAttachmentsAdding(IAttachmentsRepository.IAddEvent event) {
        addAll(event.getAttachments());
    }

    private void addAll(List<Pair<Integer, AbsModel>> data) {
        for (Pair<Integer, AbsModel> pair : data) {
            getData().add(new AttachmentEntry(true, pair.getSecond()).setOptionalId(pair.getFirst()));
        }

        if (safeCountOf(data) > 0) {
            safeNotifyDataSetChanged();
        }
    }

    private Single<List<AttachmentEntry>> attachmentsSingle() {
        return attachmentsRepository
                .getAttachmentsWithIds(getAccountId(), AttachToType.COMMENT, commentId)
                .map(pairs -> createFrom(pairs, true));
    }

    private Single<List<AttachmentEntry>> uploadsSingle() {
        return uploadManager.get(getAccountId(), destination)
                .flatMap(u -> Single.just(createFrom(u)));
    }

    private void loadAttachments() {
        appendDisposable(attachmentsSingle()
                .zipWith(uploadsSingle(), this::combine)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAttachmentsRestored, Analytics::logUnexpectedError));
    }

    private void onAttachmentsRestored(List<AttachmentEntry> entries) {
        getData().addAll(entries);

        if (nonEmpty(entries)) {
            safeNotifyDataSetChanged();
        }
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmentEntry attachment) {
        if (attachment.getOptionalId() != 0) {
            subscribeOnIOAndIgnore(attachmentsRepository.remove(getAccountId(), AttachToType.COMMENT, commentId, attachment.getOptionalId()));
            // из списка не удаляем, так как удаление из репозитория "слушается"
            // (будет удалено асинхронно и после этого удалится из списка)
        } else {
            // такого в комментах в принципе быть не может !!!
            manuallyRemoveElement(index);
        }
    }

    @Override
    protected void onModelsAdded(List<? extends AbsModel> models) {
        subscribeOnIOAndIgnore(attachmentsRepository.attach(getAccountId(), AttachToType.COMMENT, commentId, models));
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        uploadManager.enqueue(UploadUtils.createIntents(getAccountId(), destination, photos, size, true));
    }

    @Override
    protected void doUploadFile(String file, int size) {
        uploadManager.enqueue(UploadUtils.createIntents(getAccountId(), destination, file, size, true));
    }

    @Override
    public void onGuiCreated(@NonNull ICreateCommentView view) {
        super.onGuiCreated(view);

        resolveButtonsVisibility();
    }

    private void resolveButtonsVisibility() {
        callView(v -> v.setSupportedButtons(true, true, true, true, false, false));
    }

    private void returnDataToParent() {
        callView(v -> v.returnDataToParent(getTextBody()));
    }

    public void fireReadyClick() {
        callView(ICreateCommentView::goBack);
    }

    public boolean onBackPressed() {
        returnDataToParent();
        return true;
    }
}
