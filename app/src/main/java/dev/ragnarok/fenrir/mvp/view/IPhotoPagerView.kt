package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Commented
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IPhotoPagerView : IMvpView, IAccountDependencyView, IErrorView, IToastView {
    fun goToLikesList(accountId: Int, ownerId: Int, photoId: Int)
    fun setupLikeButton(visible: Boolean, like: Boolean, likes: Int)
    fun setupWithUserButton(users: Int)
    fun setupShareButton(visible: Boolean)
    fun setupCommentsButton(visible: Boolean, count: Int)
    fun displayPhotos(photos: List<Photo>, initialIndex: Int)
    fun setToolbarTitle(title: String?)
    fun setToolbarSubtitle(subtitle: String?)
    fun sharePhoto(accountId: Int, photo: Photo)
    fun postToMyWall(photo: Photo, accountId: Int)
    fun requestWriteToExternalStoragePermission()
    fun setButtonRestoreVisible(visible: Boolean)
    fun setupOptionMenu(canSaveYourself: Boolean, canDelete: Boolean)
    fun goToComments(accountId: Int, commented: Commented)
    fun displayPhotoListLoading(loading: Boolean)
    fun setButtonsBarVisible(visible: Boolean)
    fun setToolbarVisible(visible: Boolean)
    fun rebindPhotoAt(position: Int)
    fun closeOnly()
    fun returnInfo(position: Int, parcelNativePtr: Long)
    fun returnOnlyPos(position: Int)
}