package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.FeedbackVKOfficialList
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFeedbackVKOfficialView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(pages: FeedbackVKOfficialList)
    fun notifyFirstListReceived()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyItemRemoved(position: Int)
    fun showRefreshing(refreshing: Boolean)
}