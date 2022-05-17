package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IUserBannedView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayUserList(users: List<User>)
    fun notifyItemsAdded(position: Int, count: Int)
    fun notifyDataSetChanged()
    fun notifyItemRemoved(position: Int)
    fun displayRefreshing(refreshing: Boolean)
    fun startUserSelection(accountId: Int)
    fun showSuccessToast()
    fun scrollToPosition(position: Int)
    fun showUserProfile(accountId: Int, user: User)
}