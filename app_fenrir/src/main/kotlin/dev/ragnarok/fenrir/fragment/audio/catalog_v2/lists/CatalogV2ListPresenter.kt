package dev.ragnarok.fenrir.fragment.audio.catalog_v2.lists

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.model.AudioArtist
import dev.ragnarok.fenrir.model.catalog_v2_audio.CatalogV2List
import dev.ragnarok.fenrir.settings.Settings
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class CatalogV2ListPresenter(
    accountId: Int,
    private val owner_id: Int,
    private val artist_id: String?,
    private val query: String?,
    private val url: String?,
    private val context: Context,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<ICatalogV2ListView>(accountId, savedInstanceState) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.createAudioInteractor()
    private val mSections: ArrayList<CatalogV2List.CatalogV2ListItem> =
        ArrayList()
    private val netDisposable = CompositeDisposable()
    private var netLoadingNow = false
    private var dataDisposable = Disposable.disposed()

    fun resolveLoading() {
        view?.resolveLoading(netLoadingNow)
    }

    fun fireRepost(context: Context) {
        if (artist_id.isNullOrEmpty()) {
            return
        }
        netDisposable.add(
            audioInteractor.getArtistById(accountId, artist_id).fromIOToMain()
                .subscribe({ artistInfo ->
                    artistInfo.id.ifNonNullNoEmpty({
                        startForSendAttachments(context, accountId, AudioArtist(it))
                    }, {
                        startForSendAttachments(context, accountId, AudioArtist(artist_id))
                    })
                }, {
                    startForSendAttachments(context, accountId, AudioArtist(artist_id))
                })
        )
    }

    override fun onDestroyed() {
        netDisposable.dispose()
        dataDisposable.dispose()
        super.onDestroyed()
    }

    fun fireSearchRequestSubmitted(q: String?) {
        dataDisposable.dispose()
        if (q.isNullOrEmpty()) {
            return
        }
        view?.search(accountId, q)
    }

    /*
    fun fireSearchRequestChanged(q: String?) {
        dataDisposable.dispose()
        if(q.isNullOrEmpty()) {
            return
        }
        dataDisposable = Single.just(Any())
            .delay(1, TimeUnit.SECONDS)
            .fromIOToMain()
            .subscribe({ fireSearchRequestSubmitted(q) }, RxUtils.ignore())
    }
     */

    private fun request() {
        netLoadingNow = true
        resolveLoading()
        netDisposable.add(audioInteractor.getCatalogV2Sections(
            accountId,
            owner_id,
            artist_id,
            url,
            query,
            null
        )
            .fromIOToMain()
            .subscribe({ sections ->
                onNetDataReceived(
                    sections
                )
            }) { t -> onNetDataGetError(t) })
    }

    private fun onNetDataGetError(t: Throwable) {
        mSections.clear()
        addExtra()
        view?.notifyDataSetChanged()
        netLoadingNow = false
        resolveLoading()
        showError(t)
        view?.onFail()
    }

    private fun addExtra() {
        if (artist_id.nonNullNoEmpty() || query.nonNullNoEmpty() || url.nonNullNoEmpty()) {
            return
        }
        if (accountId == owner_id) {
            mSections.add(
                CatalogV2List.CatalogV2ListItem(
                    CatalogV2List.CatalogV2ListItem.TYPE_LOCAL_AUDIO,
                    context.getString(R.string.local_audios)
                )
            )
            if (accountId >= 0 && Settings.get().other().localServer.enabled) {
                mSections.add(
                    CatalogV2List.CatalogV2ListItem(
                        CatalogV2List.CatalogV2ListItem.TYPE_LOCAL_SERVER_AUDIO,
                        context.getString(R.string.on_server)
                    )
                )
            }
        }
        mSections.add(
            CatalogV2List.CatalogV2ListItem(
                CatalogV2List.CatalogV2ListItem.TYPE_AUDIO,
                context.getString(R.string.my_saved)
            )
        )
        mSections.add(
            CatalogV2List.CatalogV2ListItem(
                CatalogV2List.CatalogV2ListItem.TYPE_PLAYLIST,
                context.getString(R.string.playlists)
            )
        )
        mSections.add(
            CatalogV2List.CatalogV2ListItem(
                CatalogV2List.CatalogV2ListItem.TYPE_RECOMMENDATIONS,
                context.getString(R.string.recommendation)
            )
        )
    }

    private fun onNetDataReceived(data: CatalogV2List) {
        netLoadingNow = false
        resolveLoading()
        mSections.clear()
        mSections.addAll(data.sections.orEmpty())
        addExtra()
        view?.notifyDataSetChanged()
        var pos = 0
        for (i in 0 until data.sections?.size.orZero()) {
            if (data.sections?.get(i)?.id == data.default_section) {
                pos = i
                break
            }
        }
        view?.setSection(pos)
    }

    override fun onGuiCreated(viewHost: ICatalogV2ListView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(mSections)
        resolveLoading()
    }

    fun fireRefresh() {
        netDisposable.clear()
        netLoadingNow = false
        resolveLoading()
        request()
    }

    init {
        request()
    }
}
