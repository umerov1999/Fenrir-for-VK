package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LoadMoreState
import dev.ragnarok.fenrir.model.feedback.Feedback
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFeedbackView : IAccountDependencyView, IMvpView, IAttachmentsPlacesView, IErrorView {
    fun displayData(data: MutableList<Feedback>)
    fun showLoading(loading: Boolean)
    fun notifyDataAdding(position: Int, count: Int)
    fun notifyFirstListReceived()
    fun notifyDataSetChanged()
    fun configLoadMore(@LoadMoreState loadmoreState: Int)
    fun showLinksDialog(accountId: Int, notification: Feedback)
}