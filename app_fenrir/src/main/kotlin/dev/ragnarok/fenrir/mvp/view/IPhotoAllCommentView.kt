package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Comment
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IPhotoAllCommentView : IAccountDependencyView, IMvpView, IErrorView, IToastView,
    IAttachmentsPlacesView {
    fun displayData(comments: MutableList<Comment>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun dismissDeepLookingCommentProgress()
    fun displayDeepLookingCommentProgress()
    fun moveFocusTo(index: Int, smooth: Boolean)
    fun notifyDataAddedToTop(count: Int)
    fun notifyItemChanged(index: Int)
}