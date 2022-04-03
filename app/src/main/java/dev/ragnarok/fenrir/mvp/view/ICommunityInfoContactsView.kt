package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Manager
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICommunityInfoContactsView : IAccountDependencyView, IErrorView, IMvpView, IToastView {
    fun notifyDataSetChanged()
    fun displayRefreshing(loadingNow: Boolean)
    fun displayData(managers: List<Manager>)
    fun showUserProfile(accountId: Int, user: User)
    fun notifyItemRemoved(index: Int)
    fun notifyItemChanged(index: Int)
    fun notifyItemAdded(index: Int)
}