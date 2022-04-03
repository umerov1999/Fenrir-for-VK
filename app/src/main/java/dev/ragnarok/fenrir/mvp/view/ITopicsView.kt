package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LoadMoreState
import dev.ragnarok.fenrir.model.Topic
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ITopicsView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(topics: MutableList<Topic>)
    fun notifyDataSetChanged()
    fun notifyDataAdd(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun setupLoadMore(@LoadMoreState state: Int)
    fun goToComments(accountId: Int, topic: Topic)
}