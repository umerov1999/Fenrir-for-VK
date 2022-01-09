package dev.ragnarok.fenrir.fragment.fave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.fave.FaveLinksAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.FaveLink;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FaveLinksPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveLinksView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FaveLinksFragment extends BaseMvpFragment<FaveLinksPresenter, IFaveLinksView> implements IFaveLinksView, FaveLinksAdapter.ClickListener {

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FaveLinksAdapter mAdapter;

    public static FaveLinksFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FaveLinksFragment fragment = new FaveLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_links, container, false);

        RecyclerView recyclerView = root.findViewById(android.R.id.list);
        mEmpty = root.findViewById(R.id.empty);

        LinearLayoutManager manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(FaveLinksPresenter::fireScrollToEnd);
            }
        });

        FloatingActionButton add = root.findViewById(R.id.add_button);
        add.setOnClickListener(v -> callPresenter(p -> p.fireAdd(requireActivity())));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(FaveLinksPresenter::fireRefresh));

        mAdapter = new FaveLinksAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (Objects.nonNull(mEmpty) && Objects.nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    }

    @Override
    public void onLinkClick(int index, FaveLink link) {
        callPresenter(p -> p.fireLinkClick(link));
    }

    @Override
    public void openLink(int accountId, FaveLink link) {
        LinkHelper.openLinkInBrowser(requireActivity(), link.getUrl());
    }

    @Override
    public void notifyItemRemoved(int index) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(index);
            resolveEmptyText();
        }
    }

    @Override
    public void onLinkDelete(int index, FaveLink link) {
        callPresenter(p -> p.fireDeleteClick(link));
    }

    @Override
    public void displayLinks(List<FaveLink> links) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.setData(links);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (Objects.nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<FaveLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FaveLinksPresenter(requireArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }
}