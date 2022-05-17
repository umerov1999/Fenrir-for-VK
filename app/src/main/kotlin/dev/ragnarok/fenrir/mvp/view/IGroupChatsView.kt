package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.GroupChats
import dev.ragnarok.fenrir.model.LoadMoreState
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IGroupChatsView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(chats: MutableList<GroupChats>)
    fun notifyDataSetChanged()
    fun notifyDataAdd(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun setupLoadMore(@LoadMoreState state: Int)
    fun goToChat(accountId: Int, chat_id: Int)
}