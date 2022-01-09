package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.VideoAlbum;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IVideoAlbumsView extends IMvpView, IAccountDependencyView, IErrorView {

    void displayData(@NonNull List<VideoAlbum> data);

    void notifyDataAdded(int position, int count);

    void displayLoading(boolean loading);

    void notifyDataSetChanged();

    void openAlbum(int accountId, int ownerId, int albumId, String action, String title);
}
