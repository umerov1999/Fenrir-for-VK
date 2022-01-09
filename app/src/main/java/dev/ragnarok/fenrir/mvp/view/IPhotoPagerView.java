package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IPhotoPagerView extends IMvpView, IAccountDependencyView, IErrorView, IToastView {

    void goToLikesList(int accountId, int ownerId, int photoId);

    void setupLikeButton(boolean visible, boolean like, int likes);

    void setupWithUserButton(int users);

    void setupShareButton(boolean visible);

    void setupCommentsButton(boolean visible, int count);

    void displayPhotos(@NonNull List<Photo> photos, int initialIndex);

    void setToolbarTitle(@Nullable String title);

    void setToolbarSubtitle(@Nullable String subtitle);

    void sharePhoto(int accountId, @NonNull Photo photo);

    void postToMyWall(@NonNull Photo photo, int accountId);

    void requestWriteToExternalStoragePermission();

    void setButtonRestoreVisible(boolean visible);

    void setupOptionMenu(boolean canSaveYourself, boolean canDelete);

    void goToComments(int accountId, @NonNull Commented commented);

    void displayPhotoListLoading(boolean loading);

    void setButtonsBarVisible(boolean visible);

    void setToolbarVisible(boolean visible);

    void rebindPhotoAt(int position);

    void closeOnly();

    void returnInfo(int position, long parcelNativePtr);

    void returnOnlyPos(int position);
}
