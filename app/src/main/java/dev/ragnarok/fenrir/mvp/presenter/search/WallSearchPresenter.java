package dev.ragnarok.fenrir.mvp.presenter.search;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.db.model.PostUpdate;
import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.fragment.search.criteria.WallSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.view.search.IBaseSearchView;
import dev.ragnarok.fenrir.mvp.view.search.IWallSearchView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class WallSearchPresenter extends AbsSearchPresenter<IWallSearchView, WallSearchCriteria, Post, IntNextFrom> {

    private static final int COUNT = 30;
    private final IWallsRepository walls;

    public WallSearchPresenter(int accountId, @Nullable WallSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        walls = Repository.INSTANCE.getWalls();

        appendDisposable(walls.observeMinorChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostMinorUpdates));
    }

    private void onPostMinorUpdates(PostUpdate update) {
        for (int i = 0; i < data.size(); i++) {
            Post post = data.get(i);

            if (post.getVkid() == update.getPostId() && post.getOwnerId() == update.getOwnerId()) {
                if (nonNull(update.getLikeUpdate())) {
                    post.setLikesCount(update.getLikeUpdate().getCount());
                    post.setUserLikes(update.getLikeUpdate().isLiked());
                }

                if (nonNull(update.getDeleteUpdate())) {
                    post.setDeleted(update.getDeleteUpdate().isDeleted());
                }

                boolean pinStateChanged = false;

                if (nonNull(update.getPinUpdate())) {
                    pinStateChanged = true;

                    for (Post p : data) {
                        p.setPinned(false);
                    }

                    post.setPinned(update.getPinUpdate().isPinned());
                }

                if (pinStateChanged) {
                    callView(IBaseSearchView::notifyDataSetChanged);
                } else {
                    int finalI = i;
                    callView(view -> view.notifyItemChanged(finalI));
                }

                break;
            }
        }
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
    Single<Pair<List<Post>, IntNextFrom>> doSearch(int accountId, WallSearchCriteria criteria, IntNextFrom startFrom) {
        int offset = isNull(startFrom) ? 0 : startFrom.getOffset();
        IntNextFrom nextFrom = new IntNextFrom(offset + COUNT);

        return walls.search(accountId, criteria.getOwnerId(), criteria.getQuery(), true, COUNT, offset)
                .map(pair -> Pair.Companion.create(pair.getFirst(), nextFrom));
    }

    @Override
    WallSearchCriteria instantiateEmptyCriteria() {
        // not supported
        throw new UnsupportedOperationException();
    }

    @Override
    boolean canSearch(WallSearchCriteria criteria) {
        return Utils.trimmedNonEmpty(criteria.getQuery());
    }

    public final void fireShowCopiesClick(Post post) {
        fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_COPIES);
    }

    public final void fireShowLikesClick(Post post) {
        fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_LIKES);
    }

    public void fireLikeClick(Post post) {
        int accountId = getAccountId();

        appendDisposable(walls.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(RxUtils.ignore(), t -> callView(v -> showError(v, t))));
    }
}