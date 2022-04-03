package dev.ragnarok.fenrir.mvp.view.wallattachments

import dev.ragnarok.fenrir.model.PhotoAlbum
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IWallPhotoAlbumAttachmentsView : IAccountDependencyView, IMvpView, IErrorView,
    IAttachmentsPlacesView {
    fun displayData(albums: MutableList<PhotoAlbum>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun toolbarTitle(title: String)
    fun toolbarSubtitle(subtitle: String)
    fun onSetLoadingStatus(isLoad: Int)
}