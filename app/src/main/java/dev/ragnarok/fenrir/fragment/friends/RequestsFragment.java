package dev.ragnarok.fenrir.fragment.friends;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.FriendsRecycleAdapter;
import dev.ragnarok.fenrir.adapter.OwnersAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UsersPart;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.RequestsPresenter;
import dev.ragnarok.fenrir.mvp.view.IRequestsView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class RequestsFragment extends BaseMvpFragment<RequestsPresenter, IRequestsView>
        implements FriendsRecycleAdapter.Listener, IRequestsView {

    private FriendsRecycleAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static RequestsFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.USER_ID, userId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        RequestsFragment requestsFragment = new RequestsFragment();
        requestsFragment.setArguments(args);
        return requestsFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View root = inflater.inflate(R.layout.fragment_friends, container, false);
        RecyclerView mRecyclerView = root.findViewById(R.id.list);
        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(RequestsPresenter::fireRefresh));

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        LinearLayoutManager manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(RequestsPresenter::fireScrollToEnd);
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

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mySearchView.getWindowToken(), 0);

        mAdapter = new FriendsRecycleAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<RequestsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new RequestsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.USER_ID), saveInstanceState
        );
    }

    @Override
    public void notifyDatasetChanged(boolean enabled) {
        if (nonNull(mAdapter)) {
            mAdapter.setGroup(enabled);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setSwipeRefreshEnabled(boolean enabled) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setEnabled(enabled);
        }
    }

    @Override
    public void displayData(List<UsersPart> data, boolean grouping) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data, grouping);
        }
    }

    @Override
    public void notifyItemRangeInserted(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showUserWall(int accountId, User user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity());
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void showNotRequests(@NonNull List<Owner> data, int accountId) {
        OwnersAdapter adapter = new OwnersAdapter(requireActivity(), data);
        adapter.setClickListener(owner -> Utils.openPlaceWithSwipebleActivity(requireActivity(), PlaceFactory.getOwnerWallPlace(accountId, owner.getOwnerId(), null)));
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(requireActivity().getString(R.string.not_request))
                .setView(Utils.createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
                .setPositiveButton(R.string.button_ok, (dialog, which) -> callPresenter(RequestsPresenter::clearModificationRequests))
                .setCancelable(false)
                .show();
    }

    @Override
    public void onUserClick(User user) {
        callPresenter(p -> p.fireUserClick(user));
    }
}
