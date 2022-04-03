package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.VideoAlbum
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IVideoAlbumsByVideoView : IMvpView, IAccountDependencyView, IErrorView {
    fun displayData(data: List<VideoAlbum>)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun notifyDataSetChanged()
    fun openAlbum(accountId: Int, ownerId: Int, albumId: Int, action: String?, title: String?)
}