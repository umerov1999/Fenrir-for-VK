package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IFeedbackInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.feedback.Feedback;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IFeedbackView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FeedbackPresenter extends PlaceSupportPresenter<IFeedbackView> {

    private static final int COUNT_PER_REQUEST = 15;

    private final List<Feedback> mData;
    private final IFeedbackInteractor feedbackInteractor;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private String mNextFrom;
    private boolean actualDataReceived;
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;
    private String netLoadingStartFrom;

    public FeedbackPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        feedbackInteractor = InteractorFactory.createFeedbackInteractor();
        mData = new ArrayList<>();

        loadAllFromDb();
        requestActualData(null);
    }

    private void resolveLoadMoreFooter() {
        if (isEmpty(mData)) {
            callView(v -> v.configLoadMore(LoadMoreState.INVISIBLE));
            return;
        }

        if (nonEmpty(mData) && netLoadingNow && nonEmpty(netLoadingStartFrom)) {
            callView(v -> v.configLoadMore(LoadMoreState.LOADING));
            return;
        }

        if (canLoadMore()) {
            callView(v -> v.configLoadMore(LoadMoreState.CAN_LOAD_MORE));
            return;
        }

        callView(v -> v.configLoadMore(LoadMoreState.END_OF_LIST));
    }

    private void requestActualData(String startFrom) {
        netDisposable.clear();

        netLoadingNow = true;
        netLoadingStartFrom = startFrom;

        int accountId = getAccountId();

        resolveLoadMoreFooter();
        resolveSwipeRefreshLoadingView();

        netDisposable.add(feedbackInteractor.getActualFeedbacks(accountId, COUNT_PER_REQUEST, startFrom)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onActualDataReceived(startFrom, pair.getFirst(), pair.getSecond()), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        t.printStackTrace();

        netLoadingNow = false;
        netLoadingStartFrom = null;

        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveLoadMoreFooter();
        resolveSwipeRefreshLoadingView();
    }

    private void onActualDataReceived(String startFrom, List<Feedback> feedbacks, String nextFrom) {
        cacheDisposable.clear();
        cacheLoadingNow = false;
        netLoadingNow = false;
        netLoadingStartFrom = null;
        mNextFrom = nextFrom;
        mEndOfContent = isEmpty(nextFrom);
        actualDataReceived = true;

        if (isEmpty(startFrom)) {
            mData.clear();
            mData.addAll(feedbacks);
            callView(IFeedbackView::notifyFirstListReceived);
        } else {
            int sizeBefore = mData.size();
            mData.addAll(feedbacks);
            callView(view -> view.notifyDataAdding(sizeBefore, feedbacks.size()));
        }

        resolveLoadMoreFooter();
        resolveSwipeRefreshLoadingView();
    }

    private void resolveSwipeRefreshLoadingView() {
        callView(v -> v.showLoading(netLoadingNow && isEmpty(netLoadingStartFrom)));
    }

    private boolean canLoadMore() {
        return nonEmpty(mNextFrom) && !mEndOfContent && !cacheLoadingNow && !netLoadingNow && actualDataReceived;
    }

    @Override
    public void onGuiCreated(@NonNull IFeedbackView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mData);

        resolveLoadMoreFooter();
        resolveSwipeRefreshLoadingView();
    }

    private void loadAllFromDb() {
        cacheLoadingNow = true;
        int accountId = getAccountId();

        cacheDisposable.add(feedbackInteractor.getCachedFeedbacks(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Throwable::printStackTrace));
    }

    private void onCachedDataReceived(List<Feedback> feedbacks) {
        cacheLoadingNow = false;
        mData.clear();
        mData.addAll(feedbacks);

        callView(IFeedbackView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    public void fireItemClick(@NonNull Feedback notification) {
        callView(v -> v.showLinksDialog(getAccountId(), notification));
    }

    public void fireLoadMoreClick() {
        if (canLoadMore()) {
            requestActualData(mNextFrom);
        }
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        netDisposable.clear();
        netLoadingNow = false;
        netLoadingStartFrom = null;

        requestActualData(null);
    }

    public void fireScrollToLast() {
        if (canLoadMore()) {
            requestActualData(mNextFrom);
        }
    }
}