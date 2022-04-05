package dev.ragnarok.fenrir.mvp.presenter.search

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VkApiArtist
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.search.criteria.ArtistSearchCriteria
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.mvp.view.search.IArtistSearchView
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.core.Single

class ArtistSearchPresenter(
    accountId: Int,
    criteria: ArtistSearchCriteria?,
    savedInstanceState: Bundle?
) : AbsSearchPresenter<IArtistSearchView, ArtistSearchCriteria, VkApiArtist, IntNextFrom>(
    accountId,
    criteria,
    savedInstanceState
) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.createAudioInteractor()
    override val initialNextFrom: IntNextFrom
        get() = IntNextFrom(0)

    override fun isAtLast(startFrom: IntNextFrom): Boolean {
        return startFrom.offset == 0
    }

    override fun onSearchError(throwable: Throwable) {
        super.onSearchError(throwable)
        showError(getCauseIfRuntime(throwable))
    }

    override fun doSearch(
        accountId: Int,
        criteria: ArtistSearchCriteria,
        startFrom: IntNextFrom
    ): Single<Pair<List<VkApiArtist>, IntNextFrom>> {
        val nextFrom = IntNextFrom(startFrom.offset + 50)
        return audioInteractor.searchArtists(accountId, criteria, startFrom.offset, 50)
            .map { audio -> create(audio, nextFrom) }
    }

    fun onAdd(album: AudioPlaylist) {
        val accountId = accountId
        appendDisposable(audioInteractor.followPlaylist(
            accountId,
            album.id,
            album.ownerId,
            album.access_key
        )
            .fromIOToMain()
            .subscribe({
                view?.customToast?.showToast(R.string.success)
            }) { throwable ->
                showError(
                    throwable
                )
            })
    }

    override fun canSearch(criteria: ArtistSearchCriteria?): Boolean {
        return criteria?.query.trimmedNonNullNoEmpty()
    }

    override fun instantiateEmptyCriteria(): ArtistSearchCriteria {
        return ArtistSearchCriteria("")
    }

}