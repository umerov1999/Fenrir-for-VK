package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.model.AppChatUser
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IChatUsersDomainView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import java.util.*

class ChatUsersDomainPresenter(
    accountId: Int,
    private val chatId: Int,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IChatUsersDomainView>(accountId, savedInstanceState) {
    private val messagesInteractor: IMessagesRepository
    private val users: MutableList<AppChatUser>
    private val original: MutableList<AppChatUser>
    private var refreshing = false
    private var query: String? = null
    fun setLoadingNow(loadingNow: Boolean) {
        refreshing = loadingNow
        resolveRefreshing()
    }

    private fun updateCriteria() {
        setLoadingNow(true)
        users.clear()
        if (query.isNullOrEmpty()) {
            users.addAll(original)
            setLoadingNow(false)
            view?.notifyDataSetChanged()
            return
        }
        for (i in original) {
            val user = i.member
            if (query?.lowercase(Locale.getDefault())?.let {
                    user.fullName.lowercase(Locale.getDefault())
                        .contains(it)
                } == true || query?.lowercase(Locale.getDefault())?.let {
                    user.domain.lowercase(Locale.getDefault()).contains(
                        it
                    )
                } == true
            ) {
                users.add(i)
            }
        }
        setLoadingNow(false)
        view?.notifyDataSetChanged()
    }

    fun fireQuery(q: String?) {
        query = if (q.isNullOrEmpty()) null else {
            q
        }
        updateCriteria()
    }

    override fun onGuiCreated(viewHost: IChatUsersDomainView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(users)
    }

    private fun resolveRefreshing() {
        view?.displayRefreshing(
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
        val accountId = accountId
        setRefreshing(true)
        appendDisposable(messagesInteractor.getChatUsers(accountId, chatId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ users: List<AppChatUser> -> onDataReceived(users) }) { t: Throwable ->
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
        original.clear()
        original.addAll(users)
        updateCriteria()
    }

    fun fireRefresh() {
        if (!refreshing) {
            requestData()
        }
    }

    fun fireUserClick(user: AppChatUser) {
        view?.addDomain(
            accountId,
            user.member
        )
    }

    fun fireUserLongClick(user: AppChatUser) {
        view?.openUserWall(
            accountId,
            user.member
        )
    }

    init {
        users = ArrayList()
        original = ArrayList()
        messagesInteractor = messages
        requestData()
    }
}