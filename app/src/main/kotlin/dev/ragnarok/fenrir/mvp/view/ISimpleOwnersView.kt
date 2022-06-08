package dev.ragnarok.fenrir.mvp.view

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ISimpleOwnersView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayOwnerList(owners: List<Owner>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun displayRefreshing(refreshing: Boolean)
    fun showOwnerWall(accountId: Int, owner: Owner)
    fun updateTitle(@StringRes res: Int)
}