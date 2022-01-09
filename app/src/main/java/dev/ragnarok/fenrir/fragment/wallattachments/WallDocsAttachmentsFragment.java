package dev.ragnarok.fenrir.fragment.wallattachments;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.DocsAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.wallattachments.WallDocsAttachmentsPresenter;
import dev.ragnarok.fenrir.mvp.view.wallattachments.IWallDocsAttachmentsView;
import dev.ragnarok.fenrir.util.ViewUtils;

public class WallDocsAttachmentsFragment extends PlaceSupportMvpFragment<WallDocsAttachmentsPresenter, IWallDocsAttachmentsView>
        implements IWallDocsAttachmentsView, DocsAdapter.ActionListener {

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DocsAdapter mAdapter;
    private FloatingActionButton mLoadMore;

    public static WallDocsAttachmentsFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        WallDocsAttachmentsFragment fragment = new WallDocsAttachmentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wall_attachments, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mEmpty = root.findViewById(R.id.empty);
        mLoadMore = root.findViewById(R.id.goto_button);

        RecyclerView recyclerView = root.findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(WallDocsAttachmentsPresenter::fireScrollToEnd);
            }
        });
        mLoadMore.setOnClickListener(v -> callPresenter(WallDocsAttachmentsPresenter::fireScrollToEnd));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(WallDocsAttachmentsPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new DocsAdapter(Collections.emptyList());
        mAdapter.setActionListener(this);

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
    public void displayData(List<Document> documents) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(documents);
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

    @NonNull
    @Override
    public IPresenterFactory<WallDocsAttachmentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new WallDocsAttachmentsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }

    @Override
    public void setToolbarTitle(String title) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onSetLoadingStatus(int isLoad) {
        switch (isLoad) {
            case 1:
                mLoadMore.setImageResource(R.drawable.audio_died);
                break;
            case 2:
                mLoadMore.setImageResource(R.drawable.view);
                break;
            default:
                mLoadMore.setImageResource(R.drawable.ic_arrow_down);
                break;
        }
    }

    @Override
    public void onDocClick(int index, @NonNull Document doc) {
        callPresenter(p -> p.fireDocClick(doc));
    }

    @Override
    public boolean onDocLongClick(int index, @NonNull Document doc) {
        return false;
    }
}
