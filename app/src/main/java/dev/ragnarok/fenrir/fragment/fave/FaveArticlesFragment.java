package dev.ragnarok.fenrir.fragment.fave;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.fave.FaveArticlesAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FaveArticlesPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveArticlesView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FaveArticlesFragment extends BaseMvpFragment<FaveArticlesPresenter, IFaveArticlesView>
        implements IFaveArticlesView, SwipeRefreshLayout.OnRefreshListener, FaveArticlesAdapter.ClickListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FaveArticlesAdapter mAdapter;
    private TextView mEmpty;

    public static FaveArticlesFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FaveArticlesFragment fragment = new FaveArticlesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_articles, container, false);
        RecyclerView recyclerView = root.findViewById(android.R.id.list);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mEmpty = root.findViewById(R.id.empty);

        int columnCount = getResources().getInteger(R.integer.articles_column_count);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), columnCount));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(FaveArticlesPresenter::fireScrollToEnd);
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new FaveArticlesAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onRefresh() {
        callPresenter(FaveArticlesPresenter::fireRefresh);
    }

    @Override
    public void displayData(List<Article> articles) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(articles);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void goToArticle(int accountId, String url) {
        PlaceFactory.getExternalLinkPlace(accountId, url).tryOpenWith(requireActivity());
    }

    @Override
    public void goToPhoto(int accountId, Photo photo) {
        ArrayList<Photo> temp = new ArrayList<>(Collections.singletonList(photo));
        PlaceFactory.getSimpleGalleryPlace(accountId, temp, 0, false).tryOpenWith(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<FaveArticlesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FaveArticlesPresenter(requireArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }

    @Override
    public void onUrlClick(String url) {
        callPresenter(p -> p.fireArticleClick(url));
    }

    @Override
    public void onPhotosOpen(Photo photo) {
        callPresenter(p -> p.firePhotoClick(photo));
    }

    @Override
    public void onDelete(int index, Article article) {
        callPresenter(p -> p.fireArticleDelete(index, article));
    }

    @Override
    public void onShare(Article article) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), Settings.get().accounts().getCurrent(), article);
    }
}
