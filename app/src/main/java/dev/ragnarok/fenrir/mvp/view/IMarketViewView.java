package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IMarketViewView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayLoading(boolean loading);

    void displayMarket(Market market, int accountId);

    void sendMarket(int accountId, Market market);

    void onWriteToMarketer(int accountId, Market market, Peer peer);
}
