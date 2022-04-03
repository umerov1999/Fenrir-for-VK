package dev.ragnarok.fenrir.mvp.presenter.search

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.search.criteria.AudioPlaylistSearchCriteria
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.mvp.view.search.IAudioPlaylistSearchView
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.core.Single

class AudioPlaylistSearchPresenter(
    accountId: Int,
    criteria: AudioPlaylistSearchCriteria?,
    savedInstanceState: Bundle?
) : AbsSearchPresenter<IAudioPlaylistSearchView, AudioPlaylistSearchCriteria, AudioPlaylist, IntNextFrom>(
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
        criteria: AudioPlaylistSearchCriteria,
        startFrom: IntNextFrom
    ): Single<Pair<List<AudioPlaylist>, IntNextFrom>> {
        val nextFrom = IntNextFrom(startFrom.offset + 50)
        return audioInteractor.searchPlaylists(accountId, criteria, startFrom.offset, 50)
            .map { audio: List<AudioPlaylist> -> create(audio, nextFrom) }
    }

    fun onAdd(album: AudioPlaylist, clone: Boolean) {
        val accountId = accountId
        appendDisposable((if (clone) audioInteractor.clonePlaylist(
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
                view?.customToast?.showToast(R.string.success)
            }) { throwable: Throwable? ->
                showError(throwable)
            })
    }

    override fun canSearch(criteria: AudioPlaylistSearchCriteria?): Boolean {
        return criteria?.query.trimmedNonNullNoEmpty()
    }

    override fun instantiateEmptyCriteria(): AudioPlaylistSearchCriteria {
        return AudioPlaylistSearchCriteria("")
    }

}