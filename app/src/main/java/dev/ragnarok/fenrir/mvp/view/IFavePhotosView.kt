package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFavePhotosView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(photos: List<Photo>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun goToGallery(accountId: Int, photos: ArrayList<Photo>, position: Int)
}