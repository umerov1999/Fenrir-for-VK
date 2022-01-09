package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter
import dev.ragnarok.fenrir.mvp.view.ILocalJsonToChatView
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.PersistentLogger
import dev.ragnarok.fenrir.util.RxUtils
import dev.ragnarok.fenrir.util.Utils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.*

class LocalJsonToChatPresenter(
    accountId: Int,
    private val context: Context,
    savedInstanceState: Bundle?
) : PlaceSupportPresenter<ILocalJsonToChatView>(accountId, savedInstanceState) {
    private val mPost: ArrayList<Message> = ArrayList()
    private val mCached: ArrayList<Message> = ArrayList()
    private var AttachmentType: Int
    private var isMy: Boolean
    private var peer: Peer
    private val fInteractor: IMessagesRepository = messages
    private val actualDataDisposable = CompositeDisposable()
    override fun onGuiCreated(viewHost: ILocalJsonToChatView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(mPost)
        resolveToolbar()
    }

    var uAttachmentType: Int
        set(value) {
            AttachmentType = value
        }
        get() = AttachmentType

    private fun isAttachments(message: Message): Boolean {
        if (message.forwardMessagesCount > 0 && message.fwd != null && AttachmentType == 8)
            return true
        else if (AttachmentType == 8)
            return false
        if (!message.isHasAttachments || message.attachments == null)
            return false
        when (AttachmentType) {
            1 -> {
                return !Utils.isEmpty(message.attachments.photos)
            }
            2 -> {
                return !Utils.isEmpty(message.attachments.videos)
            }
            3 -> {
                return !Utils.isEmpty(message.attachments.docs)
            }
            4 -> {
                return !Utils.isEmpty(message.attachments.audios)
            }
            5 -> {
                return !Utils.isEmpty(message.attachments.links)
            }
            6 -> {
                return !Utils.isEmpty(message.attachments.photoAlbums)
            }
            7 -> {
                return !Utils.isEmpty(message.attachments.audioPlaylists)
            }
            9 -> {
                return !Utils.isEmpty(message.attachments.posts)
            }
        }
        return true
    }

    fun toggleAttachment() {
        view?.attachments_mode(accountId, AttachmentType)
    }

    private fun checkFWD(message: Message) {
        if (AttachmentType == 8 || message.forwardMessagesCount <= 0) {
            return
        }
        for (i in message.fwd) {
            checkFWD(i)
            if (isAttachments(i)) {
                mPost.add(i)
            }
        }
    }

    fun updateMessages(isMyTogle: Boolean): Boolean {
        if (isMyTogle) {
            isMy = !isMy
        }
        mPost.clear()
        if (AttachmentType == 0) {
            if (!isMy) {
                mPost.addAll(mCached)
            } else {
                for (i in mCached) {
                    if (i.isOut) {
                        mPost.add(i)
                    }
                }
            }
        } else {
            for (i in mCached) {
                if ((!isMy || !i.isOut) && isMy) {
                    continue
                }
                checkFWD(i)
                if (isAttachments(i)) {
                    mPost.add(i)
                }
            }
        }
        resolveToolbar()
        view?.scroll_pos(0)
        view?.notifyDataSetChanged()
        resolveRefreshingView(false)
        return isMy
    }

    private fun loadActualData() {
        resolveRefreshingView(true)
        val accountId = super.getAccountId()
        actualDataDisposable.add(fInteractor.getMessagesFromLocalJSon(accountId, context)
            .compose(RxUtils.applySingleIOToMainSchedulers())
            .subscribe({ onActualDataReceived(it) }) { onActualDataGetError(it) })
    }

    private fun onActualDataGetError(t: Throwable) {
        PersistentLogger.logThrowable("LocalJSON issues", Exception(Utils.getCauseIfRuntime(t)))
        showError(view, Utils.getCauseIfRuntime(t))
        resolveRefreshingView(false)
    }

    private fun onActualDataReceived(data: Pair<Peer, List<Message>>) {
        mPost.clear()
        mPost.addAll(data.second)
        mCached.clear()
        mCached.addAll(data.second)
        peer = data.first
        resolveToolbar()
        view?.notifyDataSetChanged()
        resolveRefreshingView(false)
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView(false)
    }

    private fun resolveRefreshingView(isLoading: Boolean) {
        resumedView?.showRefreshing(isLoading)
    }

    private fun resolveToolbar() {
        view?.setToolbarTitle(peer.title)
        view?.setToolbarSubtitle(
            getString(
                R.string.messages_in_json,
                Utils.safeCountOf(mPost)
            )
        )
        view?.displayToolbarAvatar(peer)
    }

    override fun onDestroyed() {
        actualDataDisposable.dispose()
        super.onDestroyed()
    }

    init {
        AttachmentType = 0
        isMy = false
        peer = Peer(0)
        loadActualData()
    }
}
