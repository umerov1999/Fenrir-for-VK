package dev.ragnarok.fenrir.mvp.presenter.search

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IVideosInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.search.criteria.VideoSearchCriteria
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.mvp.view.search.IVideosSearchView
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import io.reactivex.rxjava3.core.Single

class VideosSearchPresenter(
    accountId: Int,
    criteria: VideoSearchCriteria?,
    savedInstanceState: Bundle?
) : AbsSearchPresenter<IVideosSearchView, VideoSearchCriteria, Video, IntNextFrom>(
    accountId,
    criteria,
    savedInstanceState
) {
    private val videosInteractor: IVideosInteractor = InteractorFactory.createVideosInteractor()
    override val initialNextFrom: IntNextFrom
        get() = IntNextFrom(0)

    override fun isAtLast(startFrom: IntNextFrom): Boolean {
        return startFrom.offset == 0
    }

    override fun doSearch(
        accountId: Int,
        criteria: VideoSearchCriteria,
        startFrom: IntNextFrom
    ): Single<Pair<List<Video>, IntNextFrom>> {
        val offset = startFrom.offset
        val nextFrom = IntNextFrom(offset + 50)
        return videosInteractor.search(accountId, criteria, 50, offset)
            .map { videos: List<Video> -> create(videos, nextFrom) }
    }

    override fun instantiateEmptyCriteria(): VideoSearchCriteria {
        return VideoSearchCriteria("", false)
    }

    override fun canSearch(criteria: VideoSearchCriteria?): Boolean {
        return criteria?.query.trimmedNonNullNoEmpty()
    }

}