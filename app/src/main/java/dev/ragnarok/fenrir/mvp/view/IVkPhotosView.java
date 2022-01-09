package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.upload.Upload;


public interface IVkPhotosView extends IMvpView, IAccountDependencyView, IErrorView, IToolbarView {
    String ACTION_SHOW_PHOTOS = "dev.ragnarok.fenrir.ACTION_SHOW_PHOTOS";
    String ACTION_SELECT_PHOTOS = "dev.ragnarok.fenrir.ACTION_SELECT_PHOTOS";

    void displayData(List<SelectablePhotoWrapper> photos, List<Upload> uploads);

    void notifyDataSetChanged();

    void notifyPhotosAdded(int position, int count);

    void displayRefreshing(boolean refreshing);

    void notifyUploadAdded(int position, int count);

    void notifyUploadRemoved(int index);

    void setButtonAddVisible(boolean visible, boolean anim);

    void notifyUploadItemChanged(int index);

    void notifyUploadProgressChanged(int id, int progress);

    void displayGallery(int accountId, int albumId, int ownerId, @NonNull TmpSource source, int position);

    void displayGalleryUnSafe(int accountId, int albumId, int ownerId, long parcelNativePointer, int position);

    void displayDefaultToolbarTitle();

    void setDrawerPhotosSelected(boolean selected);

    void returnSelectionToParent(List<Photo> selected);

    void showSelectPhotosToast();

    void startLocalPhotosSelection();

    void startLocalPhotosSelectionIfHasPermission();

    void onToggleShowDate(boolean isShow);

    void displayToolbarSubtitle(@Nullable PhotoAlbum album, @NonNull String text);

    void scrollTo(int position);
}
