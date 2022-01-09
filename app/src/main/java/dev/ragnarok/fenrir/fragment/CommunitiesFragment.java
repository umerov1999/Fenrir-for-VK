package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.CommunitiesAdapter;
import dev.ragnarok.fenrir.adapter.OwnersAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.AppStyleable;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.DataWrapper;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommunitiesPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunitiesView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class CommunitiesFragment extends BaseMvpFragment<CommunitiesPresenter, ICommunitiesView>
        implements ICommunitiesView, MySearchView.OnQueryTextListener, CommunitiesAdapter.ActionListener, BackPressCallback, MySearchView.OnBackButtonClickListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommunitiesAdapter mAdapter;
    private MySearchView mSearchView;

    public static CommunitiesFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        CommunitiesFragment fragment = new CommunitiesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_communities, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(CommunitiesPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(CommunitiesPresenter::fireScrollToEnd);
            }
        });

        mAdapter = new CommunitiesAdapter(requireActivity(), Collections.emptyList(), new Integer[0]);
        mAdapter.setActionListener(this);

        recyclerView.setAdapter(mAdapter);

        mSearchView = root.findViewById(R.id.searchview);
        mSearchView.setOnBackButtonClickListener(this);
        mSearchView.setRightButtonVisibility(false);
        mSearchView.setOnQueryTextListener(this);

        mSearchView.setLeftIcon(R.drawable.magnify);
        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void displayData(DataWrapper<Community> own, DataWrapper<Community> filtered, DataWrapper<Community> search) {
        if (nonNull(mAdapter)) {
            List<DataWrapper<Community>> wrappers = new ArrayList<>();
            wrappers.add(own);
            wrappers.add(filtered);
            wrappers.add(search);

            Integer[] titles = {null, R.string.quick_search_title, R.string.other};
            mAdapter.setData(wrappers, titles);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.COMMUNITIES);

        ActivityUtils.setToolbarTitle(this, R.string.groups);
        ActivityUtils.setToolbarSubtitle(this, null); // TODO: 04.10.2017

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyOwnDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(0, position, count);
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void showCommunityWall(int accountId, Community community) {
        PlaceFactory.getOwnerWallPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void notifySearchDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(2, position, count);
        }
    }

    private void showNotCommunities(@NonNull List<Owner> data, int accountId) {
        OwnersAdapter adapter = new OwnersAdapter(requireActivity(), data);
        adapter.setClickListener(owner -> Utils.openPlaceWithSwipebleActivity(requireActivity(), PlaceFactory.getOwnerWallPlace(accountId, owner.getOwnerId(), null)));
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(requireActivity().getString(R.string.not_communities))
                .setView(Utils.createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
                .setPositiveButton(R.string.button_ok, (dialog, which) -> callPresenter(p -> p.clearModificationCommunities(false, true)))
                .setCancelable(false)
                .show();
    }

    @Override
    public void showModCommunities(@Nullable List<Owner> add, @Nullable List<Owner> remove, int accountId) {
        if (Utils.isEmpty(add) && Utils.isEmpty(remove)) {
            return;
        }
        if (Utils.isEmpty(add) && !Utils.isEmpty(remove)) {
            showNotCommunities(remove, accountId);
            return;
        }
        OwnersAdapter adapter = new OwnersAdapter(requireActivity(), add);
        adapter.setClickListener(owner -> Utils.openPlaceWithSwipebleActivity(requireActivity(), PlaceFactory.getOwnerWallPlace(accountId, owner.getOwnerId(), null)));
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(requireActivity().getString(R.string.new_communities))
                .setView(Utils.createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    callPresenter(p -> p.clearModificationCommunities(true, false));
                    if (!Utils.isEmpty(remove)) {
                        showNotCommunities(remove, accountId);
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void showCommunityMenu(Community community) {
        String delete = getString(R.string.delete);
        List<String> options = new ArrayList<>();
        options.add(delete);
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(community.getFullName())
                .setItems(options.toArray(new String[0]), (dialogInterface, which) -> {
                    String selected = options.get(which);
                    if (selected.equals(delete)) {
                        callPresenter(p -> p.fireUnsubscribe(community));
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @NonNull
    @Override
    public IPresenterFactory<CommunitiesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunitiesPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.USER_ID),
                saveInstanceState
        );
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        callPresenter(p -> p.fireSearchQueryChanged(query));
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        callPresenter(p -> p.fireSearchQueryChanged(newText));
        return true;
    }

    @Override
    public void onCommunityClick(Community community) {
        callPresenter(p -> p.fireCommunityClick(community));
    }

    @Override
    public boolean onCommunityLongClick(Community community) {
        return callPresenter(p -> p.fireCommunityLongClick(community), false);
    }

    @Override
    public boolean onBackPressed() {
        CharSequence query = mSearchView.getText();
        if (Utils.isEmpty(query)) {
            return true;
        }

        mSearchView.setQuery("");
        return false;
    }

    @Override
    public void onBackButtonClick() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 1 && requireActivity() instanceof AppStyleable) {
            ((AppStyleable) requireActivity()).openMenu(true);
        } else {
            requireActivity().onBackPressed();
        }
    }
}