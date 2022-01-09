package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IBoardInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ITopicsView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class TopicsPresenter extends AccountDependencyPresenter<ITopicsView> {

    private static final int COUNT_PER_REQUEST = 20;

    private final int ownerId;
    private final List<Topic> topics;
    private final IBoardInteractor boardInteractor;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean actualDataReceived;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;
    private int netLoadingNowOffset;

    public TopicsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownerId = ownerId;
        topics = new ArrayList<>();
        boardInteractor = InteractorFactory.createBoardInteractor();

        loadCachedData();
        requestActualData(0);
    }

    private void loadCachedData() {
        int accountId = getAccountId();

        cacheDisposable.add(boardInteractor.getCachedTopics(accountId, ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, RxUtils.ignore()));
    }

    private void onCachedDataReceived(List<Topic> topics) {
        cacheLoadingNow = false;

        this.topics.clear();
        this.topics.addAll(topics);

        callView(ITopicsView::notifyDataSetChanged);
    }

    private void requestActualData(int offset) {
        int accountId = getAccountId();

        netLoadingNow = true;
        netLoadingNowOffset = offset;

        resolveRefreshingView();
        resolveLoadMoreFooter();

        netDisposable.add(boardInteractor.getActualTopics(accountId, ownerId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(topics -> onActualDataReceived(offset, topics), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        callView(v -> showError(v, t));
    }

    private void onActualDataReceived(int offset, List<Topic> topics) {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        actualDataReceived = true;
        endOfContent = topics.isEmpty();

        if (offset == 0) {
            this.topics.clear();
            this.topics.addAll(topics);
            callView(ITopicsView::notifyDataSetChanged);
        } else {
            int startCount = this.topics.size();
            this.topics.addAll(topics);
            callView(view -> view.notifyDataAdd(startCount, topics.size()));
        }
    }

    @Override
    public void onGuiCreated(@NonNull ITopicsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(topics);

        resolveRefreshingView();
        resolveLoadMoreFooter();
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
    }

    private void resolveLoadMoreFooter() {
        if (netLoadingNow && netLoadingNowOffset > 0) {
            callView(v -> v.setupLoadMore(LoadMoreState.LOADING));
            return;
        }

        if (actualDataReceived && !netLoadingNow) {
            callView(v -> v.setupLoadMore(LoadMoreState.CAN_LOAD_MORE));
        }

        callView(v -> v.setupLoadMore(LoadMoreState.END_OF_LIST));
    }

    public void fireLoadMoreClick() {
        if (canLoadMore()) {
            requestActualData(topics.size());
        }
    }

    private boolean canLoadMore() {
        return actualDataReceived && !cacheLoadingNow && !endOfContent && !topics.isEmpty();
    }

    public void fireButtonCreateClick() {
        callView(v -> v.showError(R.string.not_yet_implemented_message));
    }

    public void fireRefresh() {
        netDisposable.clear();
        netLoadingNow = false;

        cacheDisposable.clear();
        cacheLoadingNow = false;

        requestActualData(0);
    }

    public void fireTopicClick(Topic topic) {
        callView(v -> v.goToComments(getAccountId(), topic));
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestActualData(topics.size());
        }
    }
}