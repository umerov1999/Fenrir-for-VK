package dev.ragnarok.fenrir.fragment.likes.reactions

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository
import dev.ragnarok.fenrir.fragment.absownerslist.ISimpleOwnersView
import dev.ragnarok.fenrir.fragment.absownerslist.SimpleOwnersPresenter
import dev.ragnarok.fenrir.model.ReactedMessagesPeers
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain

class ReactedPeersPresenter(
    accountId: Long,
    private val peerId: Long,
    private val cmid: Int,
    private val reactionId: Int?,
    savedInstanceState: Bundle?
) : SimpleOwnersPresenter<ISimpleOwnersView>(accountId, savedInstanceState) {
    private val messageRepository: IMessagesRepository = Repository.messages
    private val netDisposable = CompositeJob()
    private var endOfContent = false
    private var loadingNow = false
    private fun requestData() {
        loadingNow = true
        resolveRefreshingView()
        netDisposable.add(
            messageRepository.getReactedPeers(
                accountId,
                peerId,
                cmid,
                reactionId
            )
                .fromIOToMain({
                    onDataReceived(
                        it
                    )
                }) { onDataGetError(it) })
    }

    private fun onDataGetError(t: Throwable) {
        showError(t)
        loadingNow = false
        resolveRefreshingView()
    }

    private fun onDataReceived(reacted: List<ReactedMessagesPeers>) {
        loadingNow = false
        endOfContent = true
        data.clear()
        for (i in reacted) {
            i.owner?.let { data.add(it) }
        }
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
        if (!loadingNow && !endOfContent && data.nonNullNoEmpty()) {
            requestData()
        }
    }

    init {
        requestData()
    }
}
