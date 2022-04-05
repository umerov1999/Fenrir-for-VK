package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.AudioArtist
import dev.ragnarok.fenrir.model.AudioCatalog
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IAudioCatalogView
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class AudioCatalogPresenter(accountId: Int, artist_id: String?, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IAudioCatalogView>(accountId, savedInstanceState) {
    private val pages: MutableList<AudioCatalog>
    private val fInteractor: IAudioInteractor
    private val artist_id: String?
    private var actualDataDisposable = Disposable.disposed()
    private var query: String? = null
    private var doAudioLoadTabs = false
    private var actualDataLoading = false
    private fun do_compare(q: String?): Boolean {
        return if (q.isNullOrEmpty() && query
                .isNullOrEmpty()
        ) {
            true
        } else !query.isNullOrEmpty() && !q
            .isNullOrEmpty() && query.equals(q, ignoreCase = true)
    }

    fun fireSearchRequestChanged(q: String?) {
        if (do_compare(q)) {
            return
        }
        query = q?.trim { it <= ' ' }
        actualDataDisposable.dispose()
        if (query.isNullOrEmpty()) {
            fireRefresh()
            return
        }
        actualDataDisposable = Single.just(Any())
            .delay(1, TimeUnit.SECONDS)
            .fromIOToMain()
            .subscribe({ fireRefresh() }) { t -> onActualDataGetError(t) }
    }

    override fun onGuiCreated(viewHost: IAudioCatalogView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(pages)
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doAudioLoadTabs = if (doAudioLoadTabs) {
            return
        } else {
            true
        }
        loadActualData()
    }

    private fun loadActualData() {
        actualDataLoading = true
        resolveRefreshingView()
        val accountId = accountId
        appendDisposable(fInteractor.getCatalog(accountId, artist_id, query)
            .fromIOToMain()
            .subscribe({ data -> onActualDataReceived(data) }) { t ->
                onActualDataGetError(
                    t
                )
            })
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(
            getCauseIfRuntime(t)
        )
        resolveRefreshingView()
    }

    private fun onActualDataReceived(data: List<AudioCatalog>) {
        actualDataLoading = false
        pages.clear()
        pages.addAll(data)
        view?.notifyDataSetChanged()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.showRefreshing(
            actualDataLoading
        )
    }

    fun onAdd(album: AudioPlaylist) {
        val accountId = accountId
        appendDisposable(fInteractor.followPlaylist(
            accountId,
            album.id,
            album.ownerId,
            album.access_key
        )
            .fromIOToMain()
            .subscribe({
                view?.customToast?.showToast(
                    R.string.success
                )
            }) {
                showError(
                    it
                )
            })
    }

    override fun onDestroyed() {
        actualDataDisposable.dispose()
        super.onDestroyed()
    }

    fun fireRepost(context: Context) {
        if (artist_id.isNullOrEmpty()) {
            return
        }
        startForSendAttachments(context, accountId, AudioArtist(artist_id))
    }

    fun fireRefresh() {
        if (actualDataLoading) {
            return
        }
        loadActualData()
    }

    init {
        pages = ArrayList()
        this.artist_id = artist_id
        fInteractor = InteractorFactory.createAudioInteractor()
    }
}