package dev.ragnarok.fenrir.fragment.messages.chatmembers

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.AppChatUser
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.findIndexById
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime

class ChatMembersPresenter(accountId: Int, private val chatId: Int, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IChatMembersView>(accountId, savedInstanceState) {
    private val messagesInteractor: IMessagesRepository
    private val users: MutableList<AppChatUser>
    private var refreshing = false
    private var isOwner = false
    override fun onGuiCreated(viewHost: IChatMembersView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(users)
    }

    private fun resolveRefreshing() {
        resumedView?.displayRefreshing(
            refreshing
        )
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshing()
    }

    private fun setRefreshing(refreshing: Boolean) {
        this.refreshing = refreshing
        resolveRefreshing()
    }

    private fun requestData() {
        setRefreshing(true)
        appendDisposable(messagesInteractor.getChatUsers(accountId, chatId)
            .fromIOToMain()
            .subscribe({ onDataReceived(it) }) { t ->
                onDataGetError(
                    t
                )
            })
    }

    private fun onDataGetError(t: Throwable) {
        setRefreshing(false)
        showError(t)
    }

    private fun onDataReceived(users: List<AppChatUser>) {
        setRefreshing(false)
        this.users.clear()
        this.users.addAll(users)
        isOwner = false
        for (i in users) {
            if (i.getObjectId() == accountId) {
                isOwner = i.isOwner()
                break
            }
        }
        view?.setIsOwner(isOwner)
        view?.notifyDataSetChanged()
    }

    fun fireRefresh() {
        if (!refreshing) {
            requestData()
        }
    }

    fun fireAddUserClick() {
        view?.startSelectUsersActivity(
            accountId
        )
    }

    fun fireUserDeleteConfirmed(user: AppChatUser) {
        val userId = user.getMember()?.ownerId ?: return
        appendDisposable(messagesInteractor.removeChatMember(accountId, chatId, userId)
            .fromIOToMain()
            .subscribe({ onUserRemoved(userId) }) { t ->
                showError(getCauseIfRuntime(t))
            })
    }

    private fun onUserRemoved(id: Int) {
        val index = findIndexById(users, id)
        if (index != -1) {
            users.removeAt(index)
            view?.notifyItemRemoved(
                index
            )
        }
    }

    fun fireUserSelected(owners: ArrayList<Owner>?) {
        owners ?: return
        val users = ArrayList<User>()
        for (i in owners) {
            if (i is User) {
                users.add(i)
            }
        }
        if (users.nonNullNoEmpty()) {
            appendDisposable(messagesInteractor.addChatUsers(accountId, chatId, users)
                .fromIOToMain()
                .subscribe({ onChatUsersAdded(it) }) { t ->
                    onChatUsersAddError(
                        t
                    )
                })
        }
    }

    private fun onChatUsersAddError(t: Throwable) {
        showError(getCauseIfRuntime(t))
        requestData() // refresh data
    }

    private fun onChatUsersAdded(added: List<AppChatUser>) {
        val startSize = users.size
        users.addAll(added)
        view?.notifyDataAdded(
            startSize,
            added.size
        )
    }

    fun fireUserClick(user: AppChatUser) {
        view?.openUserWall(
            accountId,
            user.getMember() ?: return
        )
    }

    fun fireAdminToggleClick(isAdmin: Boolean, ownerId: Int) {
        appendDisposable(messagesInteractor.setMemberRole(accountId, chatId, ownerId, isAdmin)
            .fromIOToMain()
            .subscribe({ fireRefresh() }) { t -> onChatUsersAddError(t) })
    }

    init {
        users = ArrayList()
        messagesInteractor = messages
        requestData()
    }
}