package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFriendsByPhonesView : IMvpView, IAccountDependencyView, IErrorView {
    fun displayData(owners: List<Owner>)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun notifyDataSetChanged()
    fun showOwnerWall(accountId: Int, owner: Owner)
}