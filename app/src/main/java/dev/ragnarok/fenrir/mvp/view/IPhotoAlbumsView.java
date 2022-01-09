package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoAlbumEditor;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IPhotoAlbumsView extends IMvpView, IAccountDependencyView, IErrorView {

    void displayData(@NonNull List<PhotoAlbum> data);

    void displayLoading(boolean loading);

    void notifyDataSetChanged();

    void setToolbarSubtitle(String subtitle);

    void openAlbum(int accountId, @NonNull PhotoAlbum album, @Nullable Owner owner, @Nullable String action);

    void showAlbumContextMenu(@NonNull PhotoAlbum album);

    void showDeleteConfirmDialog(@NonNull PhotoAlbum album);

    void doSelection(@NonNull PhotoAlbum album);

    void setCreateAlbumFabVisible(boolean visible);

    void goToAlbumCreation(int accountId, int ownerId);

    void goToAlbumEditing(int accountId, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor);

    void seDrawertPhotoSectionActive(boolean active);

    void notifyItemRemoved(int index);

    void notifyDataAdded(int position, int size);

    void goToPhotoComments(int accountId, int ownerId);
}