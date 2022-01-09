package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface ILinksInCatalogView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Link> links);

    void notifyListChanged();

    void displayRefreshing(boolean refresing);
}
