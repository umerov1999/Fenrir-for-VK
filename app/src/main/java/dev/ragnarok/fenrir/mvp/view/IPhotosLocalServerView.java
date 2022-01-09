package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IPhotosLocalServerView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Photo> photos);

    void notifyListChanged();

    void notifyItemChanged(int index);

    void notifyDataAdded(int position, int count);

    void displayLoading(boolean loading);

    void displayGallery(int accountId, int albumId, int ownerId, @NonNull TmpSource source, int position, boolean reversed);

    void displayGalleryUnSafe(int accountId, int albumId, int ownerId, long parcelNativePointer, int position, boolean reversed);

    void scrollTo(int position);
}
