package dev.ragnarok.fenrir.mvp.presenter.wallattachments;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

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
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.wallattachments.IWallSearchCommentsAttachmentsView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class WallSearchCommentsAttachmentsPresenter extends PlaceSupportPresenter<IWallSearchCommentsAttachmentsView> {

    private final List<Comment> data;
    private final List<Integer> posts;
    private final ICommentsInteractor interactor;
    private final int owner_id;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private int loaded;
    private int offset;
    private int index;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;

    public WallSearchCommentsAttachmentsPresenter(int accountId, int ownerId, @NonNull List<Integer> posts, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.posts = posts;
        owner_id = ownerId;
        data = new ArrayList<>();
        interactor = new CommentsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
        loadActualData();
    }

    @Override
    public void onGuiCreated(@NonNull IWallSearchCommentsAttachmentsView view) {
        super.onGuiCreated(view);
        view.displayData(data);

        resolveToolbar();
    }

    private void loadActualData() {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(interactor.getCommentsNoCache(accountId, owner_id, posts.get(index), offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualDataReceived, this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void onActualDataReceived(List<Comment> res) {
        offset += 100;
        actualDataLoading = false;
        endOfContent = res.isEmpty() && index == posts.size() - 1;
        if (res.isEmpty()) {
            index++;
            offset = 0;
        }
        actualDataReceived = true;
        if (endOfContent)
            callResumedView(v -> v.onSetLoadingStatus(2));

        int startSize = data.size();
        loaded += res.size();
        data.addAll(res);
        resolveToolbar();
        callView(view -> view.notifyDataAdded(startSize, res.size() - startSize));
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(actualDataLoading));
        if (!endOfContent)
            callResumedView(v -> v.onSetLoadingStatus(actualDataLoading ? 1 : 0));
    }

    private void resolveToolbar() {
        callView(v -> {
            v.setToolbarTitle(getString(R.string.attachments_in_wall));
            v.setToolbarSubtitle(getString(R.string.comments, safeCountOf(data)) + " " + getString(R.string.comments_analized, loaded));
        });
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && actualDataReceived && !actualDataLoading) {
            loadActualData();
            return false;
        }
        return true;
    }

    public void fireRefresh() {

        actualDataDisposable.clear();
        actualDataLoading = false;

        offset = 0;
        index = 0;
        loaded = 0;
        data.clear();
        callView(IWallSearchCommentsAttachmentsView::notifyDataSetChanged);
        loadActualData();
    }

    public void fireReplyToOwnerClick(int commentId) {
        for (int y = 0; y < data.size(); y++) {
            Comment comment = data.get(y);
            if (comment.getId() == commentId) {
                comment.setAnimationNow(true);

                int finalY = y;
                callView(v -> v.notifyItemChanged(finalY));
                callView(v -> v.moveFocusTo(finalY));
                return;
            } else if (comment.hasThreads()) {
                for (int s = 0; s < comment.getThreads().size(); s++) {
                    Comment thread = comment.getThreads().get(s);
                    if (thread.getId() == commentId) {
                        thread.setAnimationNow(true);
                        int finalY = y;
                        callView(v -> v.notifyItemChanged(finalY));
                        callView(v -> v.moveFocusTo(finalY));
                        return;
                    }
                }
            }
        }
    }

    private String getApiCommentType(Comment comment) {
        switch (comment.getCommented().getSourceType()) {
            case CommentedType.PHOTO:
                return "photo_comment";
            case CommentedType.POST:
                return "comment";
            case CommentedType.VIDEO:
                return "video_comment";
            case CommentedType.TOPIC:
                return "topic_comment";
            default:
                throw new IllegalArgumentException();
        }
    }

    public void fireWhoLikesClick(Comment comment) {
        callView(v -> v.goToLikes(getAccountId(), getApiCommentType(comment), owner_id, comment.getId()));
    }

    public void fireGoCommentPostClick(Comment comment) {
        callView(v -> v.goToPost(getAccountId(), comment.getCommented().getSourceOwnerId(), comment.getCommented().getSourceId()));
    }
}
