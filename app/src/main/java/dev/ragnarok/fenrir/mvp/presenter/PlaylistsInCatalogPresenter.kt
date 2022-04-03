package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.model.CatalogBlock
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IPlaylistsInCatalogView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.disposables.CompositeDisposable

class PlaylistsInCatalogPresenter(
    accountId: Int,
    private val block_id: String,
    savedInstanceState: Bundle?
) :
    AccountDependencyPresenter<IPlaylistsInCatalogView>(accountId, savedInstanceState) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.createAudioInteractor()
    private val audios: ArrayList<AudioPlaylist> = ArrayList()
    private val audioListDisposable = CompositeDisposable()
    private var actualReceived = false
    private var next_from: String? = null
    private var loadingNow = false
    private var endOfContent = false
    private var doAudioLoadTabs = false
    fun setLoadingNow(loadingNow: Boolean) {
        this.loadingNow = loadingNow
        resolveRefreshingView()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doAudioLoadTabs = if (doAudioLoadTabs) {
            return
        } else {
            true
        }
        fireRefresh()
    }

    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(
            loadingNow
        )
    }

    fun requestList() {
        setLoadingNow(true)
        audioListDisposable.add(audioInteractor.getCatalogBlockById(accountId, block_id, next_from)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ onListReceived(it) }) { t: Throwable ->
                onListGetError(
                    t
                )
            })
    }

    private fun onListReceived(data: CatalogBlock?) {
        if (data == null || data.playlists.isNullOrEmpty()) {
            actualReceived = true
            setLoadingNow(false)
            endOfContent = true
            return
        }
        if (next_from.isNullOrEmpty()) {
            audios.clear()
        }
        next_from = data.next_from
        endOfContent = next_from.isNullOrEmpty()
        actualReceived = true
        setLoadingNow(false)
        audios.addAll(data.playlists)
        view?.notifyListChanged()
    }

    override fun onDestroyed() {
        audioListDisposable.dispose()
        super.onDestroyed()
    }

    private fun onListGetError(t: Throwable) {
        setLoadingNow(false)
        showError(
            getCauseIfRuntime(t)
        )
    }

    fun fireRefresh() {
        audioListDisposable.clear()
        next_from = null
        requestList()
    }

    fun fireScrollToEnd() {
        if (actualReceived && !endOfContent) {
            requestList()
        }
    }

    fun onAdd(album: AudioPlaylist, clone: Boolean) {
        val accountId = accountId
        audioListDisposable.add((if (clone) audioInteractor.clonePlaylist(
            accountId,
            album.id,
            album.ownerId
        ) else audioInteractor.followPlaylist(
            accountId,
            album.id,
            album.ownerId,
            album.access_key
        ))
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                view?.customToast?.showToast(
                    R.string.success
                )
            }) { throwable: Throwable? ->
                showError(throwable)
            })
    }

    override fun onGuiCreated(viewHost: IPlaylistsInCatalogView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(audios)
    }

}