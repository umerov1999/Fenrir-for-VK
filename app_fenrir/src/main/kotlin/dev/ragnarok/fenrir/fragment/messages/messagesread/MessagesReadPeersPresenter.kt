package dev.ragnarok.fenrir.fragment.messages.messagesread

import android.os.Bundle
import dev.ragnarok.fenrir.domain.Repository
import dev.ragnarok.fenrir.fragment.absownerslist.ISimpleOwnersView
import dev.ragnarok.fenrir.fragment.absownerslist.SimpleOwnersPresenter
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain

class MessagesReadPeersPresenter(
    accountId: Long,
    private val peerId: Long,
    private val cmId: Int,
    savedInstanceState: Bundle?
) : SimpleOwnersPresenter<ISimpleOwnersView>(accountId, savedInstanceState) {
    private val messageRepository = Repository.messages
    private val netDisposable = CompositeJob()
    private var endOfContent = false
    private var loadingNow = false

    private fun requestData() {
        loadingNow = true
        resolveRefreshingView()
        netDisposable.add(
            messageRepository.getMessageReadPeers(
                accountId,
                peerId,
                cmId
            )
                .fromIOToMain({
                    onDataReceived(
                        it
                    )
                }) { t -> onDataGetError(t) })
    }

    private fun onDataGetError(t: Throwable) {
        showError(t)
        loadingNow = false
        resolveRefreshingView()
    }

    private fun onDataReceived(owners: List<Owner>) {
        loadingNow = false
        endOfContent = true
        data.clear()
        data.addAll(owners)
        view?.notifyDataSetChanged()
        resolveRefreshingView()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(
            loadingNow
        )
    }

    override fun onDestroyed() {
        netDisposable.cancel()
        super.onDestroyed()
    }

    override fun onUserRefreshed() {
        netDisposable.clear()
        requestData()
    }

    override fun onUserScrolledToEnd() {

    }

    init {
        requestData()
    }
}