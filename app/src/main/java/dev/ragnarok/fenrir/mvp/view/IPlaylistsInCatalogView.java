package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IPlaylistsInCatalogView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<AudioPlaylist> audios);

    void notifyListChanged();

    void displayRefreshing(boolean refresing);
}
