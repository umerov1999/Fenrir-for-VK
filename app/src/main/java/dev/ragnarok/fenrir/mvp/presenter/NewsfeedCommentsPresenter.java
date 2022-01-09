package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

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


public class NewsfeedCommentsPresenter extends PlaceSupportPresenter<INewsfeedCommentsView> {

    private final List<NewsfeedComment> data;
    private final INewsfeedInteractor interactor;
    private String nextFrom;
    private boolean loadingNow;

    public NewsfeedCommentsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        data = new ArrayList<>();
        interactor = InteractorFactory.createNewsfeedInteractor();

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

        load(null);
    }

    private void load(String startFrom) {
        appendDisposable(interactor.getNewsfeedComments(getAccountId(), 10, startFrom, "post,photo,video,topic")
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onDataReceived(startFrom, pair.getSecond(), pair.getFirst()), this::onRequestError));
    }

    private void loadNext() {
        setLoadingNow(true);

        String startFrom = nextFrom;
        load(startFrom);
    }

    private void onRequestError(Throwable throwable) {
        callView(v -> showError(v, getCauseIfRuntime(throwable)));
        setLoadingNow(false);
    }

    private void onDataReceived(String startFrom, String newNextFrom, List<NewsfeedComment> comments) {
        setLoadingNow(false);

        boolean atLast = isEmpty(startFrom);
        nextFrom = newNextFrom;

        if (atLast) {
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
        return nonEmpty(nextFrom) && !loadingNow;
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            loadNext();
        }
    }

    public void fireRefresh() {
        if (loadingNow) {
            return;
        }

        loadAtLast();
    }

    public void fireCommentBodyClick(NewsfeedComment newsfeedComment) {
        Comment comment = newsfeedComment.getComment();
        AssertUtils.requireNonNull(comment);

        callView(v -> v.openComments(getAccountId(), comment.getCommented(), null));
    }
}