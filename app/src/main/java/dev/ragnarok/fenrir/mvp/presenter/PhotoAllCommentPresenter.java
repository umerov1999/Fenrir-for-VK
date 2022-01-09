package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.RxUtils.dummy;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.domain.ICommentsInteractor;
import dev.ragnarok.fenrir.domain.IPhotosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.impl.CommentsInteractor;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.AccessIdPair;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IPhotoAllCommentView;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.DisposableHolder;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class PhotoAllCommentPresenter extends PlaceSupportPresenter<IPhotoAllCommentView> {

    private static final int COUNT_PER_REQUEST = 25;
    private final IPhotosInteractor photosInteractor;
    private final ICommentsInteractor interactor;
    private final ArrayList<Comment> mComments;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final DisposableHolder<Void> deepLookingHolder = new DisposableHolder<>();
    private final int owner_id;
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;

    public PhotoAllCommentPresenter(int accountId, int owner_id, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.owner_id = owner_id;

        photosInteractor = InteractorFactory.createPhotosInteractor();
        interactor = new CommentsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
        mComments = new ArrayList<>();

        requestAtLast();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
    }

    @Override
    public void onDestroyed() {
        netDisposable.dispose();
        deepLookingHolder.dispose();
        super.onDestroyed();
    }

    private void request(int offset) {
        netLoadingNow = true;
        resolveRefreshingView();

        int accountId = getAccountId();

        netDisposable.add(photosInteractor.getAllComments(accountId, owner_id, null, offset, COUNT_PER_REQUEST)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(PhotoAllComment -> onNetDataReceived(offset, PhotoAllComment), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        callView(v -> showError(v, t));
    }

    private void onNetDataReceived(int offset, List<Comment> comments) {
        cacheLoadingNow = false;

        mEndOfContent = comments.isEmpty();
        netLoadingNow = false;

        if (offset == 0) {
            mComments.clear();
            mComments.addAll(comments);
            callView(IPhotoAllCommentView::notifyDataSetChanged);
        } else {
            int startSize = mComments.size();
            mComments.addAll(comments);
            callView(view -> view.notifyDataAdded(startSize, comments.size()));
        }

        resolveRefreshingView();
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mComments.size());
    }

    @Override
    public void onGuiCreated(@NonNull IPhotoAllCommentView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mComments);

        resolveRefreshingView();
    }

    private boolean canLoadMore() {
        return !mComments.isEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    public void fireCommentLikeClick(Comment comment, boolean add) {
        likeInternal(add, comment);
    }

    public void fireGoPhotoClick(Comment comment) {
        appendDisposable(photosInteractor.getPhotosByIds(getAccountId(), Collections.singletonList(new AccessIdPair(comment.getCommented().getSourceId(), owner_id, null)))
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> callView(v -> v.openSimplePhotoGallery(getAccountId(), new ArrayList<>(t), 0, false)), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void likeInternal(boolean add, Comment comment) {
        int accountId = getAccountId();

        appendDisposable(interactor.like(accountId, comment.getCommented(), comment.getId(), add)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(dummy(), t -> callView(v -> showError(v, t))));
    }

    public void fireReport(Comment comment, Context context) {
        CharSequence[] items = {"Спам", "Детская порнография", "Экстремизм", "Насилие", "Пропаганда наркотиков", "Материал для взрослых", "Оскорбление", "Призывы к суициду"};
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.report)
                .setItems(items, (dialog, item) -> {
                    appendDisposable(interactor.reportComment(getAccountId(), comment.getFromId(), comment.getId(), item)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(p -> {
                                if (p == 1)
                                    callView(v -> v.getCustomToast().showToast(R.string.success));
                                else
                                    callView(v -> v.getCustomToast().showToast(R.string.error));
                            }, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
                    dialog.dismiss();
                })
                .show();
    }

    public void fireWhoLikesClick(Comment comment) {
        callView(v -> v.goToLikes(getAccountId(), "photo_comment", comment.getCommented().getSourceOwnerId(), comment.getId()));
    }

    public void fireReplyToChat(Comment comment, Context context) {
        SendAttachmentsActivity.startForSendAttachments(context, getAccountId(), new WallReply().buildFromComment(comment, comment.getCommented()));
    }

    public void fireReplyToOwnerClick(int commentId) {
        for (int y = 0; y < mComments.size(); y++) {
            Comment comment = mComments.get(y);
            if (comment.getId() == commentId) {
                comment.setAnimationNow(true);

                int finalY = y;
                callView(v -> {
                    v.notifyItemChanged(finalY);
                    v.moveFocusTo(finalY, true);
                });
                return;
            }
        }

        //safeShowToast(getView(), R.string.the_comment_is_not_in_the_list, false);

        startDeepCommentFinding(commentId);
    }

    @Nullable
    private Comment getFirstCommentInList() {
        return nonEmpty(mComments) ? mComments.get(mComments.size() - 1) : null;
    }

    private void startDeepCommentFinding(int commentId) {
        if (netLoadingNow || cacheLoadingNow) {
            // не грузить, если сейчас что-то грузится
            return;
        }

        Comment older = getFirstCommentInList();
        AssertUtils.requireNonNull(older);

        int accountId = getAccountId();

        callView(IPhotoAllCommentView::displayDeepLookingCommentProgress);

        deepLookingHolder.append(interactor.getAllCommentsRange(accountId, older.getCommented(), older.getId(), commentId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(comments -> onDeepCommentLoadingResponse(commentId, comments), this::onDeepCommentLoadingError));
    }

    private void onDeepCommentLoadingError(Throwable throwable) {
        callView(IPhotoAllCommentView::dismissDeepLookingCommentProgress);

        if (throwable instanceof NotFoundException) {
            callView(v -> v.getCustomToast().showToast(R.string.the_comment_is_not_in_the_list));
        } else {
            callView(v -> showError(v, throwable));
        }
    }

    private void onDeepCommentLoadingResponse(int commentId, List<Comment> comments) {
        callView(IPhotoAllCommentView::dismissDeepLookingCommentProgress);

        mComments.addAll(comments);

        int index = -1;
        for (int i = 0; i < mComments.size(); i++) {
            Comment comment = mComments.get(i);

            if (comment.getId() == commentId) {
                index = i;
                comment.setAnimationNow(true);
                break;
            }
        }

        if (index == -1) {
            return;
        }

        callView(view -> view.notifyDataAddedToTop(comments.size()));

        int finalIndex = index;
        callView(view -> view.moveFocusTo(finalIndex, false));
    }

    @Override
    public void onGuiDestroyed() {
        deepLookingHolder.dispose();
        super.onGuiDestroyed();
    }

    public void fireDeepLookingCancelledByUser() {
        deepLookingHolder.dispose();
    }
}
