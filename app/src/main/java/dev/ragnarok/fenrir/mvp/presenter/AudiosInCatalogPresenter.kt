package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.CatalogBlock
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IAudiosInCatalogView
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AudiosInCatalogPresenter(
    accountId: Int,
    private val block_id: String,
    savedInstanceState: Bundle?
) :
    AccountDependencyPresenter<IAudiosInCatalogView>(accountId, savedInstanceState) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.createAudioInteractor()
    private val audios: ArrayList<Audio> = ArrayList()
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
            .fromIOToMain()
            .subscribe({ onListReceived(it) }) { t ->
                onListGetError(
                    t
                )
            })
    }

    private fun onListReceived(data: CatalogBlock?) {
        if (data == null || data.audios.isNullOrEmpty()) {
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
        audios.addAll(data.audios)
        view?.notifyListChanged()
    }

    fun playAudio(context: Context, position: Int) {
        startForPlayList(context, audios, position, false)
        if (!Settings.get().other().isShow_mini_player) getPlayerPlace(accountId).tryOpenWith(
            context
        )
    }

    override fun onDestroyed() {
        audioListDisposable.dispose()
        super.onDestroyed()
    }

    private fun onListGetError(t: Throwable) {
        setLoadingNow(false)
        showError(getCauseIfRuntime(t))
    }

    fun getAudioPos(audio: Audio?): Int {
        if (!audios.isNullOrEmpty() && audio != null) {
            for ((pos, i) in audios.withIndex()) {
                if (i.id == audio.id && i.ownerId == audio.ownerId) {
                    i.isAnimationNow = true
                    view?.notifyItemChanged(
                        pos
                    )
                    return pos
                }
            }
        }
        return -1
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

    override fun onGuiCreated(viewHost: IAudiosInCatalogView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(audios)
    }

}