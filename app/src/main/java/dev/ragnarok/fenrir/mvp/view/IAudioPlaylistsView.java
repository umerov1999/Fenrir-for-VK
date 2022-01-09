package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IAudioPlaylistsView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<AudioPlaylist> pages);

    void notifyDataSetChanged();

    void notifyItemRemoved(int position);

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void doAddAudios(int accountId);

    void showHelper();
}
