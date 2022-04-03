package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.PhotoAlbum
import dev.ragnarok.fenrir.model.PhotoAlbumEditor
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IPhotoAlbumsView : IMvpView, IAccountDependencyView, IErrorView {
    fun displayData(data: List<PhotoAlbum>)
    fun displayLoading(loading: Boolean)
    fun notifyDataSetChanged()
    fun setToolbarSubtitle(subtitle: String?)
    fun openAlbum(accountId: Int, album: PhotoAlbum, owner: Owner?, action: String?)
    fun showAlbumContextMenu(album: PhotoAlbum)
    fun showDeleteConfirmDialog(album: PhotoAlbum)
    fun doSelection(album: PhotoAlbum)
    fun setCreateAlbumFabVisible(visible: Boolean)
    fun goToAlbumCreation(accountId: Int, ownerId: Int)
    fun goToAlbumEditing(accountId: Int, album: PhotoAlbum, editor: PhotoAlbumEditor)
    fun seDrawertPhotoSectionActive(active: Boolean)
    fun notifyItemRemoved(index: Int)
    fun notifyDataAdded(position: Int, size: Int)
    fun goToPhotoComments(accountId: Int, ownerId: Int)
}