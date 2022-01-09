package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.INewsfeedInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.NewsfeedComment;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.INewsfeedCommentsView;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.RxUtils;

public class NewsfeedMentionsPresenter extends PlaceSupportPresenter<INewsfeedCommentsView> {

    private final List<NewsfeedComment> data;
    private final INewsfeedInteractor interactor;
    private final int ownerId;
    private boolean isEndOfContent;
    private boolean loadingNow;
    private int offset;

    public NewsfeedMentionsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        data = new ArrayList<>();
        interactor = InteractorFactory.createNewsfeedInteractor();
        this.ownerId = ownerId;
        offset = 0;
        loadAtLast();
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveLoadingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveLoadingView();
    }

    private void resolveLoadingView() {
        callResumedView(v -> v.showLoading(loadingNow));
    }

    private void loadAtLast() {
        setLoadingNow(true);

        load(0);
    }

    private void load(int offset) {
        appendDisposable(interactor.getMentions(getAccountId(), ownerId, 50, offset, null, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onDataReceived(offset, pair.getFirst()), this::onRequestError));
    }

    private void onRequestError(Throwable throwable) {
        callView(v -> showError(v, getCauseIfRuntime(throwable)));
        setLoadingNow(false);
    }

    private void onDataReceived(int offset, List<NewsfeedComment> comments) {
        setLoadingNow(false);
        this.offset = offset + 50;
        isEndOfContent = comments.isEmpty();

        if (offset == 0) {
            data.clear();
            data.addAll(comments);
            callView(INewsfeedCommentsView::notifyDataSetChanged);
        } else {
            int startCount = data.size();
            data.addAll(comments);
            callView(view -> view.notifyDataAdded(startCount, comments.size()));
        }
    }

    @Override
    public void onGuiCreated(@NonNull INewsfeedCommentsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(data);
    }

    private boolean canLoadMore() {
        return !isEndOfContent && !loadingNow;
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            load(offset);
        }
    }

    public void fireRefresh() {
        if (loadingNow) {
            return;
        }
        offset = 0;
        loadAtLast();
    }

    public void fireCommentBodyClick(NewsfeedComment newsfeedComment) {
        Comment comment = newsfeedComment.getComment();
        AssertUtils.requireNonNull(comment);

        callView(v -> v.openComments(getAccountId(), comment.getCommented(), null));
    }
}
