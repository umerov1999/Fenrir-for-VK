package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Narratives
import dev.ragnarok.fenrir.model.Story
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface INarrativesView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(narratives: List<Narratives>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun onNarrativesOpen(accountId: Int, stories: ArrayList<Story>)
}
