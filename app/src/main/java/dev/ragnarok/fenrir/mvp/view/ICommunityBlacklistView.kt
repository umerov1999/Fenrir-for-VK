package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Banned
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICommunityBlacklistView : IAccountDependencyView, IErrorView, IMvpView, IToastView {
    fun displayRefreshing(loadingNow: Boolean)
    fun notifyDataSetChanged()
    fun diplayData(data: List<Banned>)
    fun notifyItemRemoved(index: Int)
    fun openBanEditor(accountId: Int, groupId: Int, banned: Banned)
    fun startSelectProfilesActivity(accountId: Int, groupId: Int)
    fun addUsersToBan(accountId: Int, groupId: Int, users: ArrayList<User>)
    fun notifyItemsAdded(position: Int, size: Int)
}