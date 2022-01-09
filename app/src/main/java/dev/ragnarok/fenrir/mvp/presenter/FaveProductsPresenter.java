package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveProductsView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class FaveProductsPresenter extends AccountDependencyPresenter<IFaveProductsView> {

    private static final int COUNT_PER_REQUEST = 25;
    private final IFaveInteractor faveInteractor;
    private final ArrayList<Market> mMarkets;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;
    private boolean doLoadTabs;

    public FaveProductsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        faveInteractor = InteractorFactory.createFaveInteractor();
        mMarkets = new ArrayList<>();

        loadCachedData();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doLoadTabs) {
            return;
        } else {
            doLoadTabs = true;
        }
        requestAtLast();
    }

    private void loadCachedData() {
        cacheLoadingNow = true;

        int accountId = getAccountId();
        cacheDisposable.add(faveInteractor.getCachedProducts(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheGetError));
    }

    private void onCacheGetError(Throwable t) {
        cacheLoadingNow = false;
        callView(v -> showError(v, t));
    }

    private void onCachedDataReceived(List<Market> markets) {
        cacheLoadingNow = false;

        mMarkets.clear();
        mMarkets.addAll(markets);
        callView(IFaveProductsView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void request(int offset) {
        netLoadingNow = true;
        resolveRefreshingView();

        int accountId = getAccountId();

        netDisposable.add(faveInteractor.getProducts(accountId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(products -> onNetDataReceived(offset, products), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        callView(v -> showError(v, t));
    }

    private void onNetDataReceived(int offset, List<Market> markets) {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        mEndOfContent = markets.isEmpty();
        netLoadingNow = false;

        if (offset == 0) {
            mMarkets.clear();
            mMarkets.addAll(markets);
            callView(IFaveProductsView::notifyDataSetChanged);
        } else {
            int startSize = mMarkets.size();
            mMarkets.addAll(markets);
            callView(view -> view.notifyDataAdded(startSize, markets.size()));
        }

        resolveRefreshingView();
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mMarkets.size());
    }

    @Override
    public void onGuiCreated(@NonNull IFaveProductsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mMarkets);
    }

    private boolean canLoadMore() {
        return !mMarkets.isEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireMarketOpen(Market market) {
        callView(v -> v.onMarketOpen(getAccountId(), market));
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }
}
