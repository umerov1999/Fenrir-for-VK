package dev.ragnarok.fenrir.mvp.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IProductAlbumsView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class ProductAlbumsPresenter extends AccountDependencyPresenter<IProductAlbumsView> {

    private static final int COUNT_PER_REQUEST = 25;
    private final IOwnersRepository ownerInteractor;
    private final ArrayList<MarketAlbum> mMarkets;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final int owner_id;
    private final Context context;
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;

    public ProductAlbumsPresenter(int accountId, int owner_id, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.owner_id = owner_id;
        this.context = context;

        ownerInteractor = Repository.INSTANCE.getOwners();
        mMarkets = new ArrayList<>();

        requestAtLast();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
    }

    @Override
    public void onDestroyed() {
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void request(int offset) {
        netLoadingNow = true;
        resolveRefreshingView();

        int accountId = getAccountId();

        netDisposable.add(ownerInteractor.getMarketAlbums(accountId, owner_id, Math.max(offset - 1, 0), COUNT_PER_REQUEST)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(products -> onNetDataReceived(offset, products), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        callView(v -> showError(v, t));
    }

    private void onNetDataReceived(int offset, List<MarketAlbum> markets) {
        cacheLoadingNow = false;

        mEndOfContent = markets.isEmpty();
        netLoadingNow = false;

        if (offset == 0) {
            mMarkets.clear();
            mMarkets.add(new MarketAlbum(0, owner_id).setTitle(context.getString(R.string.markets_all)));
            mMarkets.addAll(markets);
            callView(IProductAlbumsView::notifyDataSetChanged);
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
    public void onGuiCreated(@NonNull IProductAlbumsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mMarkets);

        resolveRefreshingView();
    }

    private boolean canLoadMore() {
        return !mMarkets.isEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireAlbumOpen(MarketAlbum market_album) {
        callView(v -> v.onMarketAlbumOpen(getAccountId(), market_album));
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }
}
