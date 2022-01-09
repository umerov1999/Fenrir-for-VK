package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SelectProfilesActivity;
import dev.ragnarok.fenrir.adapter.FeedAdapter;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalOptionsAdapter;
import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.model.FeedSource;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.News;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FeedPresenter;
import dev.ragnarok.fenrir.mvp.view.IFeedView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper;

public class FeedFragment extends PlaceSupportMvpFragment<FeedPresenter, IFeedView> implements IFeedView,
        SwipeRefreshLayout.OnRefreshListener, FeedAdapter.ClickListener, HorizontalOptionsAdapter.Listener<FeedSource>, HorizontalOptionsAdapter.CustomListener<FeedSource> {

    private final Gson mGson = new Gson();
    private final ActivityResultLauncher<Intent> requestProfileSelect = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ArrayList<Owner> owners = result.getData().getParcelableArrayListExtra(Extra.OWNERS);
                    AssertUtils.requireNonNull(owners);
                    postPresenterReceive(presenter -> presenter.fireAddToFaveList(requireActivity(), owners));
                }
            });
    private FeedAdapter mAdapter;
    private TextView mEmptyText;
    private RecyclerView mRecycleView;
    private RecyclerView.LayoutManager mFeedLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoadMoreFooterHelper mLoadMoreFooterHelper;
    private HorizontalOptionsAdapter<FeedSource> mFeedSourceAdapter;
    private LinearLayoutManager mHeaderLayoutManager;

    public static Bundle buildArgs(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static FeedFragment newInstance(int accountId) {
        return newInstance(buildArgs(accountId));
    }

    public static FeedFragment newInstance(Bundle args) {
        FeedFragment feedFragment = new FeedFragment();
        feedFragment.setArguments(args);
        return feedFragment;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_feed, menu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            callPresenter(FeedPresenter::fireRefresh);
            return true;
        } else if (item.getItemId() == R.id.action_create_list) {
            requestProfileSelect.launch(SelectProfilesActivity.startFaveSelection(requireActivity()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void scrollTo(int pos) {
        mFeedLayoutManager.scrollToPosition(pos);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_new_feed, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        styleSwipeRefreshLayoutWithCurrentTheme(mSwipeRefreshLayout, false);

        if (Utils.is600dp(requireActivity())) {
            boolean land = Utils.isLandscape(requireActivity());
            mFeedLayoutManager = new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mFeedLayoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        }

        mRecycleView = root.findViewById(R.id.fragment_feeds_list);
        mRecycleView.setLayoutManager(mFeedLayoutManager);
        mRecycleView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        mRecycleView.addOnScrollListener(new EndlessRecyclerOnScrollListener(4, 1000) {
            @Override
            public void onScrollToLastElement() {
                callPresenter(FeedPresenter::fireScrollToBottom);
            }
        });

        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        Goto.setOnClickListener(v -> {
            mRecycleView.stopScroll();
            mRecycleView.scrollToPosition(0);
        });
        Goto.setOnLongClickListener(v -> {
            mRecycleView.stopScroll();
            mFeedLayoutManager.scrollToPosition(0);
            callPresenter(FeedPresenter::fireRefresh);
            return true;
        });

        mEmptyText = root.findViewById(R.id.fragment_feeds_empty_text);

        ViewGroup footerView = (ViewGroup) inflater.inflate(R.layout.footer_load_more, mRecycleView, false);

        mLoadMoreFooterHelper = LoadMoreFooterHelper.createFrom(footerView, () -> callPresenter(FeedPresenter::fireLoadMoreClick));

        //ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.header_feed, mRecycleView, false);
        RecyclerView headerRecyclerView = root.findViewById(R.id.header_list);

        mHeaderLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false);
        headerRecyclerView.setLayoutManager(mHeaderLayoutManager);

        mAdapter = new FeedAdapter(requireActivity(), Collections.emptyList(), this);

        mAdapter.setClickListener(this);
        mAdapter.addFooter(footerView);
        //mAdapter.addHeader(headerView);

        mRecycleView.setAdapter(mAdapter);

        mFeedSourceAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
        mFeedSourceAdapter.setListener(this);
        mFeedSourceAdapter.setDeleteListener(this);
        headerRecyclerView.setAdapter(mFeedSourceAdapter);
        return root;
    }

    /*private void resolveEmptyText() {
        if(!isAdded()) return;
        mEmptyText.setVisibility(!nowRequest() && safeIsEmpty(mData) ? View.VISIBLE : View.INVISIBLE);
        mEmptyText.setText(R.string.feeds_empty_text);
    }*/

   /* private void loadFeedSourcesData() {
        restoreCurrentSourceIds();

        initFeedSources(null);
        mFeedListsLoader.loadAll(getAccountId());
        executeRequest(FeedRequestFactory.getFeedListsRequest());
    }*/

    /*private void loadFeedData() {
        // восстанавливаем из настроек последнее значение next_from, данные до которого
        // хранятся в базе данных
        mNextFrom = Settings.get()
                .other()
                .restoreFeedNextFrom(getAccountId());

        // загружаем все новости, которые сохранены в базу данных текущего аккаунта
        mFeedLoader.load(buildCriteria(), true);

        // если же предыдущего состояния next_from нет, то запрашиваем новости
        // с сервера (в противном случае ничего не загружаем с сервиса, пользователь
        // должен ПРОДОЛЖИТЬ читать ленту с того места, где закончил)
        if (TextUtils.isEmpty(mNextFrom)) {
            requestFeed();
        }
    }*/

    @Override
    public void onAvatarClick(int ownerId) {
        super.onOwnerClick(ownerId);
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.FEED);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.feed);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_FEED);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onOwnerClick(int ownerId) {
        onOpenOwner(ownerId);
    }

    @Override
    public void onRepostClick(News news) {
        callPresenter(p -> p.fireNewsRepostClick(news));
    }

    @Override
    public void onPostClick(News news) {
        callPresenter(p -> p.fireNewsBodyClick(news));
    }

    @Override
    public void onBanClick(News news) {
        callPresenter(p -> p.fireBanClick(news));
    }

    @Override
    public void onIgnoreClick(News news) {
        callPresenter(p -> p.fireIgnoreClick(news));
    }

    @Override
    public void onFaveClick(News news) {
        callPresenter(p -> p.fireAddBookmark(news.getSourceId(), news.getPostId()));
    }

    @Override
    public void onCommentButtonClick(News news) {
        callPresenter(p -> p.fireNewsCommentClick(news));
    }

    @Override
    public void onLikeClick(News news, boolean add) {
        callPresenter(p -> p.fireLikeClick(news));
    }

    @Override
    public boolean onLikeLongClick(News news) {
        callPresenter(p -> p.fireNewsLikeLongClick(news));
        return true;
    }

    @Override
    public boolean onShareLongClick(News news) {
        callPresenter(p -> p.fireNewsShareLongClick(news));
        return true;
    }

    @Override
    public void goToLikes(int accountId, String type, int ownerId, int id) {
        PlaceFactory.getLikesCopiesPlace(accountId, type, ownerId, id, ILikesInteractor.FILTER_LIKES).tryOpenWith(requireActivity());
    }

    @Override
    public void goToReposts(int accountId, String type, int ownerId, int id) {
        PlaceFactory.getLikesCopiesPlace(accountId, type, ownerId, id, ILikesInteractor.FILTER_COPIES).tryOpenWith(requireActivity());
    }

    @Override
    public void goToPostComments(int accountId, int postId, int ownerId) {
        Commented commented = new Commented(postId, ownerId, CommentedType.POST, null);
        PlaceFactory.getCommentsPlace(accountId, commented, null).tryOpenWith(requireActivity());
    }

    @Override
    public void askToReload() {
        if (getView() == null) {
            return;
        }
        Snackbar.make(requireView(), R.string.update_news, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.do_update, v -> {
            mFeedLayoutManager.scrollToPosition(0);
            callPresenter(FeedPresenter::fireRefresh);
        }).show();
    }

    @Override
    public void onRefresh() {
        callPresenter(FeedPresenter::fireRefresh);
    }

    private void restoreRecycleViewManagerState(String state) {
        if (nonEmpty(state)) {
            if (mFeedLayoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager.SavedState savedState = gson().fromJson(state, LinearLayoutManager.SavedState.class);
                mFeedLayoutManager.onRestoreInstanceState(savedState);
            }

            if (mFeedLayoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.SavedState savedState = gson().fromJson(state, StaggeredGridLayoutManager.SavedState.class);
                mFeedLayoutManager.onRestoreInstanceState(savedState);
            }
        }
    }

    private Gson gson() {
        return mGson;
    }

    @Override
    public void onPause() {
        Parcelable parcelable = mFeedLayoutManager.onSaveInstanceState();
        String json = gson().toJson(parcelable);

        callPresenter(p -> p.fireScrollStateOnPause(json));
        super.onPause();
    }

    /*@Override
    public void onFeedListsLoadFinished(List<VkApiFeedList> result) {
        initFeedSources(result);
        int selected = refreshFeedSourcesSelection();

        if (mFeedSourceAdapter != null) {
            mFeedSourceAdapter.notifyDataSetChanged();
        }

        if (selected != -1 && mHeaderLayoutManager != null) {
            mHeaderLayoutManager.scrollToPosition(selected);
        }
    }*/

    /*@Override
    protected void beforeAccountChange(int oldAid, int newAid) {
        super.beforeAccountChange(oldAid, newAid);
        mFeedLoader.stopIfLoading();
        mFeedListsLoader.stopIfLoading();

        storeListPosition();
        ignoreAll();
    }*/

    /*@Override
    protected void afterAccountChange(int oldAid, int newAid) {
        super.afterAccountChange(oldAid, newAid);
        restoreCurrentSourceIds();

        mEndOfContent = false;
        mData.clear();
        mFeedSources.clear();

        mAdapter.notifyDataSetChanged();
        mFeedSourceAdapter.notifyDataSetChanged();

        loadFeedData();
        loadFeedSourcesData();

        resolveEmptyText();
        resolveFooter();
    }*/

    @Override
    public void onOptionClick(FeedSource entry) {
        mRecycleView.stopScroll();
        mRecycleView.scrollToPosition(0);
        callPresenter(p -> p.fireFeedSourceClick(entry));
    }

    @Override
    public void onDeleteOptionClick(FeedSource entry, int position) {
        Utils.ThemedSnack(requireView(), R.string.do_delete, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                v1 -> {
                    mFeedSourceAdapter.removeChild(position);
                    callPresenter(p -> p.fireFeedSourceDelete(Integer.parseInt(entry.getValue().replace("list", ""))));
                }).show();
    }

    @Override
    public void showSuccessToast() {
        Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayFeedSources(List<FeedSource> sources) {
        if (nonNull(mFeedSourceAdapter)) {
            mFeedSourceAdapter.setItems(sources);
        }
    }

    @Override
    public void notifyFeedSourcesChanged() {
        if (nonNull(mFeedSourceAdapter)) {
            mFeedSourceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayFeed(List<News> data, @Nullable String rawScrollState) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(data);
        }

        if (nonEmpty(rawScrollState) && nonNull(mFeedLayoutManager)) {
            try {
                restoreRecycleViewManagerState(rawScrollState);
            } catch (Exception ignored) {
            }
        }

        resolveEmptyTextVisibility();
    }

    @Override
    public void notifyFeedDataChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }

        resolveEmptyTextVisibility();
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position + mAdapter.getHeadersCount(), count);
        }

        resolveEmptyTextVisibility();
    }

    @Override
    public void notifyItemChanged(int position) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(position + mAdapter.getHeadersCount());
        }
    }

    @Override
    public void setupLoadMoreFooter(@LoadMoreState int state) {
        if (nonNull(mLoadMoreFooterHelper)) {
            mLoadMoreFooterHelper.switchToState(state);
        }
    }

    public void resolveEmptyTextVisibility() {
        if (nonNull(mEmptyText) && nonNull(mAdapter)) {
            mEmptyText.setVisibility(mAdapter.getRealItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void scrollFeedSourcesToPosition(int position) {
        if (nonNull(mHeaderLayoutManager)) {
            mHeaderLayoutManager.scrollToPosition(position);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<FeedPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FeedPresenter(requireArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }
}
