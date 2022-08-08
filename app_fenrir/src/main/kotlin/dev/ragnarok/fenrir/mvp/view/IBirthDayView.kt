package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.BirthDay
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IBirthDayView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(users: List<BirthDay>)
    fun notifyDataSetChanged()
    fun showRefreshing(refreshing: Boolean)
    fun goToWall(accountId: Int, user: User)
    fun moveTo(pos: Int)
}
