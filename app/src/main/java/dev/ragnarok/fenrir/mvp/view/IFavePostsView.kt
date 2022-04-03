package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFavePostsView : IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    fun displayData(posts: MutableList<Post>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun notifyItemChanged(index: Int)
}