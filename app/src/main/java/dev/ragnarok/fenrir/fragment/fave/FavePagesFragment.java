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

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.fave.FavePagesAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FavePagesPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveUsersView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class FavePagesFragment extends BaseMvpFragment<FavePagesPresenter, IFaveUsersView> implements IFaveUsersView, FavePagesAdapter.ClickListener {

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FavePagesAdapter mAdapter;

    public static FavePagesFragment newInstance(int accountId, boolean isUser) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putBoolean(Extra.USER, isUser);
        FavePagesFragment fragment = new FavePagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_pages, container, false);
        mEmpty = root.findViewById(R.id.empty);

        RecyclerView recyclerView = root.findViewById(R.id.list);
        int columns = getResources().getInteger(R.integer.photos_column_count);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), columns);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(FavePagesPresenter::fireScrollToEnd);
            }
        });

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callPresenter(p -> p.fireSearchRequestChanged(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callPresenter(p -> p.fireSearchRequestChanged(newText));
                return false;
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(FavePagesPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new FavePagesAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);

        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayData(List<FavePage> users) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(users);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void openOwnerWall(int accountId, Owner owner) {
        PlaceFactory.getOwnerWallPlace(accountId, owner).tryOpenWith(requireActivity());
    }

    @Override
    public void openMention(int accountId, Owner owner) {
        PlaceFactory.getMentionsPlace(accountId, owner.getOwnerId()).tryOpenWith(requireActivity());
    }

    @Override
    public void notifyItemRemoved(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(index);
            resolveEmptyText();
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<FavePagesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FavePagesPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getBoolean(Extra.USER),
                saveInstanceState
        );
    }

    @Override
    public void onPageClick(int index, Owner owner) {
        callPresenter(p -> p.fireOwnerClick(owner));
    }

    @Override
    public void onDelete(int index, Owner owner) {
        callPresenter(p -> p.fireOwnerDelete(owner));
    }

    @Override
    public void onPushFirst(int index, Owner owner) {
        callPresenter(p -> p.firePushFirst(owner));
    }

    @Override
    public void onMention(@NonNull Owner owner) {
        callPresenter(p -> p.fireMention(owner));
    }
}
