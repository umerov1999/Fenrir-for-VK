package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.ContactConversation
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IFriendsByPhonesView : IMvpView, IAccountDependencyView, IErrorView {
    fun displayData(owners: List<ContactConversation>)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun notifyDataSetChanged()
    fun showChat(accountId: Int, owner: ContactConversation)
}