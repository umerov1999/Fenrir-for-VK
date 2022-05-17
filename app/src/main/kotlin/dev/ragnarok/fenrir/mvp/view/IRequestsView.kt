package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.model.UsersPart
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IRequestsView : IMvpView, IErrorView, IAccountDependencyView {
    fun notifyDatasetChanged(grouping: Boolean)
    fun setSwipeRefreshEnabled(enabled: Boolean)
    fun displayData(data: List<UsersPart>, grouping: Boolean)
    fun notifyItemRangeInserted(position: Int, count: Int)
    fun showUserWall(accountId: Int, user: User)
    fun showRefreshing(refreshing: Boolean)
    fun showNotRequests(data: List<Owner>, accountId: Int)
}