package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.AppChatUser
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IChatUsersDomainView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(users: List<AppChatUser>)
    fun notifyItemRemoved(position: Int)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun openUserWall(accountId: Int, user: Owner)
    fun addDomain(accountId: Int, user: Owner)
    fun displayRefreshing(refreshing: Boolean)
}