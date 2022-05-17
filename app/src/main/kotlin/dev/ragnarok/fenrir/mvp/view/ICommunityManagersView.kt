package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Manager
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICommunityManagersView : IAccountDependencyView, IErrorView, IMvpView, IToastView {
    fun notifyDataSetChanged()
    fun displayRefreshing(loadingNow: Boolean)
    fun displayData(managers: List<Manager>)
    fun goToManagerEditing(accountId: Int, groupId: Int, manager: Manager)
    fun showUserProfile(accountId: Int, user: User)
    fun startSelectProfilesActivity(accountId: Int, groupId: Int)
    fun startAddingUsersToManagers(accountId: Int, groupId: Int, users: ArrayList<User>)
    fun notifyItemRemoved(index: Int)
    fun notifyItemChanged(index: Int)
    fun notifyItemAdded(index: Int)
}