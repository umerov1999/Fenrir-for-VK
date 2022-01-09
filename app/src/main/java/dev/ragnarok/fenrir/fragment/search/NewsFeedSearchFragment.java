package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.WallAdapter;
import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.NewsFeedSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.INewsFeedSearchView;
import dev.ragnarok.fenrir.util.Utils;

public class NewsFeedSearchFragment extends AbsSearchFragment<NewsFeedSearchPresenter, INewsFeedSearchView, Post, WallAdapter>
        implements WallAdapter.ClickListener, INewsFeedSearchView {

    public static NewsFeedSearchFragment newInstance(int accountId, @Nullable NewsFeedCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        NewsFeedSearchFragment fragment = new NewsFeedSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(WallAdapter adapter, List<Post> data) {
        adapter.setItems(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    WallAdapter createAdapter(List<Post> data) {
        return new WallAdapter(requireActivity(), data, this, this);
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        if (Utils.is600dp(requireActivity())) {
            boolean land = Utils.isLandscape(requireActivity());
            return new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            return new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        }
    }

    @Override
    public void onAvatarClick(int ownerId) {
        callPresenter(p -> p.fireOwnerClick(ownerId));
    }

    @Override
    public void onShareClick(Post post) {
        callPresenter(p -> p.fireShareClick(post));
    }

    @Override
    public void onPostClick(Post post) {
        callPresenter(p -> p.firePostClick(post));
    }

    @Override
    public void onRestoreClick(Post post) {
        // not supported
    }

    @Override
    public void onCommentsClick(Post post) {
        callPresenter(p -> p.fireCommentsClick(post));
    }

    @Override
    public void onLikeLongClick(Post post) {
        callPresenter(p -> p.fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_LIKES));
    }

    @Override
    public void onShareLongClick(Post post) {
        callPresenter(p -> p.fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_COPIES));
    }

    @Override
    public void onLikeClick(Post post) {
        callPresenter(p -> p.fireLikeClick(post));
    }

    @NonNull
    @Override
    public IPresenterFactory<NewsFeedSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new NewsFeedSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }
}