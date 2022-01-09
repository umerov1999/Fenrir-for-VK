package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IVideosInCatalogView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Video> videos);

    void notifyListChanged();

    void displayRefreshing(boolean refresing);
}
