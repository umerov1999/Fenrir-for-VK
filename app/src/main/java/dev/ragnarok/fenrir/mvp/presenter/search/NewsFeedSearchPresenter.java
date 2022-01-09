package dev.ragnarok.fenrir.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.db.model.PostUpdate;
import dev.ragnarok.fenrir.domain.IFeedInteractor;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.StringNextFrom;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.view.search.INewsFeedSearchView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class NewsFeedSearchPresenter extends AbsSearchPresenter<INewsFeedSearchView, NewsFeedCriteria, Post, StringNextFrom> {

    private final IFeedInteractor feedInteractor;

    private final IWallsRepository walls;

    public NewsFeedSearchPresenter(int accountId, @Nullable NewsFeedCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        feedInteractor = InteractorFactory.createFeedInteractor();
        walls = Repository.INSTANCE.getWalls();

        appendDisposable(walls.observeMinorChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostUpdate, RxUtils.ignore()));
    }

    private void onPostUpdate(PostUpdate update) {
        // TODO: 03.10.2017
    }

    @Override
    StringNextFrom getInitialNextFrom() {
        return new StringNextFrom(null);
    }

    @Override
    boolean isAtLast(StringNextFrom startFrom) {
        return Utils.isEmpty(startFrom.getNextFrom());
    }

    @Override
    Single<Pair<List<Post>, StringNextFrom>> doSearch(int accountId, NewsFeedCriteria criteria, StringNextFrom startFrom) {
        return feedInteractor.search(accountId, criteria, 50, startFrom.getNextFrom())
                .map(pair -> Pair.Companion.create(pair.getFirst(), new StringNextFrom(pair.getSecond())));
    }

    @Override
    NewsFeedCriteria instantiateEmptyCriteria() {
        return new NewsFeedCriteria("");
    }

    @Override
    public void firePostClick(@NonNull Post post) {
        if (post.getPostType() == VKApiPost.Type.REPLY) {
            callView(v -> v.openComments(getAccountId(), Commented.from(post), post.getVkid()));
        } else {
            callView(v -> v.openPost(getAccountId(), post));
        }
    }

    @Override
    boolean canSearch(NewsFeedCriteria criteria) {
        return Utils.nonEmpty(criteria.getQuery());
    }

    public void fireLikeClick(Post post) {
        int accountId = getAccountId();

        appendDisposable(walls.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(RxUtils.ignore(), t -> callView(v -> showError(v, t))));
    }
}