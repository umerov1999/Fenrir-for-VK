package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IProductAlbumsView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<MarketAlbum> market_albums);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void onMarketAlbumOpen(int accountId, MarketAlbum market_album);
}
