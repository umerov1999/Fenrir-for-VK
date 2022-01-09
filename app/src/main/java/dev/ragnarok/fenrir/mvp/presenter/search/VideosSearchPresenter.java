package dev.ragnarok.fenrir.mvp.presenter.search;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.domain.IVideosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.criteria.VideoSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.view.search.IVideosSearchView;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public class VideosSearchPresenter extends AbsSearchPresenter<IVideosSearchView, VideoSearchCriteria, Video, IntNextFrom> {

    private final IVideosInteractor videosInteractor;

    public VideosSearchPresenter(int accountId, @Nullable VideoSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        videosInteractor = InteractorFactory.createVideosInteractor();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Video>, IntNextFrom>> doSearch(int accountId, VideoSearchCriteria criteria, IntNextFrom startFrom) {
        int offset = startFrom.getOffset();
        IntNextFrom nextFrom = new IntNextFrom(offset + 50);
        return videosInteractor.search(accountId, criteria, 50, offset)
                .map(videos -> Pair.Companion.create(videos, nextFrom));
    }

    @Override
    VideoSearchCriteria instantiateEmptyCriteria() {
        return new VideoSearchCriteria("", false);
    }

    @Override
    boolean canSearch(VideoSearchCriteria criteria) {
        return nonEmpty(criteria.getQuery());
    }
}