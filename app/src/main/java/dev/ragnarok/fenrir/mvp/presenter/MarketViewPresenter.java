package dev.ragnarok.fenrir.mvp.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IMarketViewView;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.util.RxUtils;

public class MarketViewPresenter extends AccountDependencyPresenter<IMarketViewView> {

    private final IFaveInteractor faveInteractor;
    private final IOwnersRepository ownerInteractor;
    private Market mMarket;
    private boolean loadingNow;

    public MarketViewPresenter(int accountId, @NonNull Market market, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        mMarket = market;
        faveInteractor = InteractorFactory.createFaveInteractor();
        ownerInteractor = Repository.INSTANCE.getOwners();

        refreshPollData();
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveLoadingView();
    }

    private void refreshPollData() {
        if (loadingNow) return;

        int accountId = getAccountId();

        setLoadingNow(true);
        Collection<AccessIdPair> ids = Collections.singletonList(new AccessIdPair(mMarket.getId(), mMarket.getOwner_id(), mMarket.getAccess_key()));
        appendDisposable(ownerInteractor.getMarketById(accountId, ids)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onMarketInfoUpdated, this::onLoadingError));
    }

    private void onLoadingError(Throwable t) {
        callView(v -> showError(v, t));
        setLoadingNow(false);
    }

    private void onMarketInfoUpdated(List<Market> market) {
        if (market.size() <= 0) {
            return;
        }
        mMarket = market.get(0);
        setLoadingNow(false);
        resolveMarketView();
    }

    private void resolveLoadingView() {
        callView(v -> v.displayLoading(loadingNow));
    }

    private void resolveMarketView() {
        callView(v -> v.displayMarket(mMarket, getAccountId()));
    }

    @Override
    public void onGuiCreated(@NonNull IMarketViewView view) {
        super.onGuiCreated(view);

        resolveLoadingView();
        resolveMarketView();
    }

    public void fireSendMarket(Market market) {
        callView(v -> v.sendMarket(getAccountId(), market));
    }

    public void fireWriteToMarketer(Market market, Context context) {
        appendDisposable(OwnerInfo.getRx(context, getAccountId(), market.getOwner_id())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(userInfo -> {
                    Peer peer = new Peer(Peer.fromOwnerId(userInfo.getOwner().getOwnerId()))
                            .setAvaUrl(userInfo.getOwner().getMaxSquareAvatar())
                            .setTitle(userInfo.getOwner().getFullName());
                    callView(v -> v.onWriteToMarketer(getAccountId(), market, peer));
                }, throwable -> {
                    Peer peer = new Peer(Peer.fromOwnerId(market.getOwner_id()))
                            .setAvaUrl(market.getThumb_photo())
                            .setTitle(market.getTitle());
                    callView(v -> v.onWriteToMarketer(getAccountId(), market, peer));
                }));
    }

    private void onFaveSuccess() {
        mMarket.setIs_favorite(!mMarket.isIs_favorite());
        resolveMarketView();
    }

    public void fireFaveClick() {
        if (!mMarket.isIs_favorite()) {
            appendDisposable(faveInteractor.addProduct(getAccountId(), mMarket.getId(), mMarket.getOwner_id(), mMarket.getAccess_key())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(this::onFaveSuccess, this::onLoadingError));
        } else {
            appendDisposable(faveInteractor.removeProduct(getAccountId(), mMarket.getId(), mMarket.getOwner_id())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> onFaveSuccess(), this::onLoadingError));
        }
    }
}
