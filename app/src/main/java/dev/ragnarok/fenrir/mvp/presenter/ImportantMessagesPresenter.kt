package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.mvp.view.IImportantMessagesView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.getSelected
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ImportantMessagesPresenter(accountId: Int, savedInstanceState: Bundle?) :
    AbsMessageListPresenter<IImportantMessagesView>(accountId, savedInstanceState) {
    private val fInteractor: IMessagesRepository = messages
    private val actualDataDisposable = CompositeDisposable()
    private var actualDataReceived = false
    private var endOfContent = false
    private var actualDataLoading = false
    private fun loadActualData(offset: Int) {
        actualDataLoading = true
        resolveRefreshingView()
        val accountId = accountId
        actualDataDisposable.add(fInteractor.getImportantMessages(accountId, 50, offset, null)
            .fromIOToMain()
            .subscribe({ data ->
                onActualDataReceived(
                    offset,
                    data
                )
            }) { t -> onActualDataGetError(t) })
    }

    private fun onActualDataReceived(offset: Int, recv: List<Message>) {
        actualDataLoading = false
        endOfContent = data.isEmpty()
        actualDataReceived = true
        if (offset == 0) {
            data.clear()
            data.addAll(recv)
            safeNotifyDataChanged()
        } else {
            val startSize = data.size
            data.addAll(recv)
            view?.notifyDataAdded(
                startSize,
                recv.size
            )
        }
        resolveRefreshingView()
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(getCauseIfRuntime(t))
        resolveRefreshingView()
    }

    override fun onActionModeForwardClick() {
        super.onActionModeForwardClick()
        val selected: ArrayList<Message> = getSelected(data)
        if (selected.isNotEmpty()) {
            view?.forwardMessages(
                accountId,
                selected
            )
        }
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.showRefreshing(
            actualDataLoading
        )
    }

    fun fireScrollToEnd(): Boolean {
        if (!endOfContent && data.nonNullNoEmpty() && actualDataReceived && !actualDataLoading) {
            loadActualData(data.size)
            return false
        }
        return true
    }

    fun fireRemoveImportant(position: Int) {
        val msg = data[position]
        appendDisposable(fInteractor.markAsImportant(accountId, msg.peerId, setOf(msg.id), 0)
            .fromIOToMain()
            .subscribe({
                data.removeAt(position)
                safeNotifyDataChanged()
            }) { })
    }

    fun fireRefresh() {
        actualDataDisposable.clear()
        actualDataLoading = false
        loadActualData(0)
    }

    fun fireMessagesLookup(message: Message) {
        view?.goToMessagesLookup(
            accountId,
            message.peerId,
            message.id
        )
    }

    fun fireTranscript(voiceMessageId: String?, messageId: Int) {
        appendDisposable(fInteractor.recogniseAudioMessage(accountId, messageId, voiceMessageId)
            .fromIOToMain()
            .subscribe({ }) { })
    }

    init {
        loadActualData(0)
    }
}