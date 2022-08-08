package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IUserBannedView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayOwnerList(owners: List<Owner>)
    fun notifyItemsAdded(position: Int, count: Int)
    fun notifyDataSetChanged()
    fun notifyItemRemoved(position: Int)
    fun displayRefreshing(refreshing: Boolean)
    fun startUserSelection(accountId: Int)
    fun showSuccessToast()
    fun scrollToPosition(position: Int)
    fun showOwnerProfile(accountId: Int, owner: Owner)
}