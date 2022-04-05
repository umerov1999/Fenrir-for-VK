package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.Includes.blacklistRepository
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import dev.ragnarok.fenrir.domain.IAccountsInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.BannedPart
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IUserBannedView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Utils.findIndexById
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime

class UserBannedPresenter(accountId: Int, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IUserBannedView>(accountId, savedInstanceState) {
    private val interactor: IAccountsInteractor = InteractorFactory.createAccountInteractor()
    private val users: MutableList<User>
    private var endOfContent = false
    private var loadinNow = false
    private fun onUserRemoved(id: Int) {
        val index = findIndexById(users, id)
        if (index != -1) {
            users.removeAt(index)
            view?.notifyItemRemoved(
                index
            )
        }
    }

    private fun onUserAdded(user: User) {
        users.add(0, user)
        view?.let {
            it.notifyItemsAdded(0, 1)
            it.scrollToPosition(0)
        }
    }

    override fun onGuiCreated(viewHost: IUserBannedView) {
        super.onGuiCreated(viewHost)
        viewHost.displayUserList(users)
    }

    private fun onBannedPartReceived(offset: Int, part: BannedPart) {
        setLoadinNow(false)
        endOfContent = part.users.isEmpty()
        if (offset == 0) {
            users.clear()
            users.addAll(part.users)
            view?.notifyDataSetChanged()
        } else {
            val startSize = users.size
            users.addAll(part.users)
            view?.notifyItemsAdded(
                startSize,
                part.users.size
            )
        }
        endOfContent = endOfContent || part.totalCount == users.size
    }

    private fun onBannedPartGetError(throwable: Throwable) {
        setLoadinNow(false)
        showError(throwable)
    }

    private fun setLoadinNow(loadinNow: Boolean) {
        this.loadinNow = loadinNow
        resolveRefreshingView()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(
            loadinNow
        )
    }

    private fun loadNextPart(offset: Int) {
        if (loadinNow) return
        val accountId = accountId
        setLoadinNow(true)
        appendDisposable(interactor.getBanned(accountId, 50, offset)
            .fromIOToMain()
            .subscribe(
                { part: BannedPart -> onBannedPartReceived(offset, part) }
            ) { throwable -> onBannedPartGetError(getCauseIfRuntime(throwable)) })
    }

    fun fireRefresh() {
        loadNextPart(0)
    }

    fun fireButtonAddClick() {
        view?.startUserSelection(accountId)
    }

    private fun onAddingComplete() {
        view?.showSuccessToast()
    }

    private fun onAddError(throwable: Throwable) {
        showError(throwable)
    }

    fun fireUsersSelected(owners: ArrayList<Owner>) {
        val accountId = accountId
        val users = ArrayList<User>()
        for (i in owners) {
            if (i is User) {
                users.add(i)
            }
        }
        if (users.nonNullNoEmpty()) {
            appendDisposable(interactor.banUsers(accountId, users)
                .fromIOToMain()
                .subscribe({ onAddingComplete() }) { throwable ->
                    onAddError(
                        getCauseIfRuntime(throwable)
                    )
                })
        }
    }

    fun fireScrollToEnd() {
        if (!loadinNow && !endOfContent) {
            loadNextPart(users.size)
        }
    }

    private fun onRemoveComplete() {
        view?.showSuccessToast()
    }

    private fun onRemoveError(throwable: Throwable) {
        showError(throwable)
    }

    fun fireRemoveClick(user: User) {
        val accountId = accountId
        appendDisposable(interactor.unbanUser(accountId, user.id)
            .fromIOToMain()
            .subscribe({ onRemoveComplete() }) { throwable ->
                onRemoveError(
                    getCauseIfRuntime(throwable)
                )
            })
    }

    fun fireUserClick(user: User) {
        view?.showUserProfile(
            accountId,
            user
        )
    }

    init {
        users = ArrayList()
        loadNextPart(0)
        val repository = blacklistRepository
        appendDisposable(repository.observeAdding()
            .filter { it.first == accountId }
            .map(Pair<Int, User>::second)
            .observeOn(provideMainThreadScheduler())
            .subscribe { onUserAdded(it) })
        appendDisposable(repository.observeRemoving()
            .filter { it.first == accountId }
            .map(Pair<Int, Int>::second)
            .observeOn(provideMainThreadScheduler())
            .subscribe { onUserRemoved(it) })
    }
}