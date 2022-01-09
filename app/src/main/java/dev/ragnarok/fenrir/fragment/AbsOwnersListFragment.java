package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

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

import com.google.android.material.textview.MaterialTextView;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.OwnersAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.SimpleOwnersPresenter;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.ViewUtils;


public abstract class AbsOwnersListFragment<P extends SimpleOwnersPresenter<V>, V extends ISimpleOwnersView> extends BaseMvpFragment<P, V> implements ISimpleOwnersView {

    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected OwnersAdapter mOwnersAdapter;
    protected LinearLayoutManager mLinearLayoutManager;
    private MaterialTextView mCount;

    protected abstract boolean hasToolbar();

    protected abstract boolean needShowCount();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(hasToolbar() ? R.layout.fragment_abs_friends_with_toolbar : R.layout.fragment_abs_friends, container, false);

        if (hasToolbar()) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        }

        mRecyclerView = root.findViewById(R.id.list);
        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mCount = root.findViewById(R.id.count_data);
        if (nonNull(mCount)) {
            mCount.setVisibility(needShowCount() ? View.VISIBLE : View.GONE);
        }
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(SimpleOwnersPresenter::fireRefresh));

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mLinearLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(SimpleOwnersPresenter::fireScrollToEnd);
            }
        });

        mOwnersAdapter = new OwnersAdapter(requireActivity(), Collections.emptyList());
        mOwnersAdapter.setClickListener(owner -> callPresenter(p -> p.fireOwnerClick(owner)));
        mOwnersAdapter.setLongClickListener(this::onLongClick);

        mRecyclerView.setAdapter(mOwnersAdapter);
        return root;
    }

    protected boolean onLongClick(Owner owner) {
        return false;
    }

    @Override
    public void displayOwnerList(List<Owner> owners) {
        if (nonNull(mOwnersAdapter)) {
            mOwnersAdapter.setItems(owners);
            if (nonNull(mCount)) {
                mCount.setText(getString(R.string.people_count, owners.size()));
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mOwnersAdapter)) {
            mOwnersAdapter.notifyDataSetChanged();
            if (nonNull(mCount)) {
                mCount.setText(getString(R.string.people_count, mOwnersAdapter.getItemCount()));
            }
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mOwnersAdapter)) {
            mOwnersAdapter.notifyItemRangeInserted(position, count);
            if (nonNull(mCount)) {
                mCount.setText(getString(R.string.people_count, mOwnersAdapter.getItemCount()));
            }
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void showOwnerWall(int accountId, Owner owner) {
        PlaceFactory.getOwnerWallPlace(accountId, owner).tryOpenWith(requireActivity());
    }
}