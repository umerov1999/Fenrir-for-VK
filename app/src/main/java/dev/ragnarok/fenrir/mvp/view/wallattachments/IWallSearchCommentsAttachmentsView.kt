package dev.ragnarok.fenrir.mvp.view.wallattachments

import dev.ragnarok.fenrir.model.Comment
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IWallSearchCommentsAttachmentsView : IAccountDependencyView, IMvpView, IErrorView,
    IAttachmentsPlacesView {
    fun displayData(comments: MutableList<Comment>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun toolbarTitle(title: String)
    fun toolbarSubtitle(subtitle: String)
    fun onSetLoadingStatus(isLoad: Int)
    fun moveFocusTo(index: Int)
    fun notifyItemChanged(index: Int)
    fun goToPost(accountId: Int, ownerId: Int, postId: Int)
}