package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.FriendsCounters
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFriendsTabsView : IMvpView, IAccountDependencyView, IErrorView {
    fun displayCounters(counters: FriendsCounters)
    fun configTabs(accountId: Int, userId: Int, isNotMyPage: Boolean)
    fun displayUserNameAtToolbar(userName: String?)
    fun setDrawerFriendsSectionSelected(selected: Boolean)
}