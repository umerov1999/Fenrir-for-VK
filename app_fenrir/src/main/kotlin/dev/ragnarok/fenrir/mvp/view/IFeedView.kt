package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.FeedSource
import dev.ragnarok.fenrir.model.LoadMoreState
import dev.ragnarok.fenrir.model.News
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFeedView : IAccountDependencyView, IAttachmentsPlacesView, IMvpView, IErrorView {
    fun displayFeedSources(sources: MutableList<FeedSource>)
    fun notifyFeedSourcesChanged()
    fun displayFeed(data: MutableList<News>, rawScrollState: String?)
    fun notifyFeedDataChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyItemChanged(position: Int)
    fun setupLoadMoreFooter(@LoadMoreState state: Int)
    fun showRefreshing(refreshing: Boolean)
    fun scrollFeedSourcesToPosition(position: Int)
    fun scrollTo(pos: Int)
    override fun goToLikes(accountId: Int, type: String?, ownerId: Int, id: Int)
    override fun goToReposts(accountId: Int, type: String?, ownerId: Int, id: Int)
    fun goToPostComments(accountId: Int, postId: Int, ownerId: Int)
    fun showSuccessToast()
    fun askToReload()
}