package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.ShortLink;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IShortedLinksView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<ShortLink> links);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void updateLink(String url);

    void showLinkStatus(String status);
}
