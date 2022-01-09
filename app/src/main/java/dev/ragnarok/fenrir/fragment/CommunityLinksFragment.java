package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.CommunityLinksAdapter;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommunityLinksPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityLinksView;
import dev.ragnarok.fenrir.util.ViewUtils;

public class CommunityLinksFragment extends BaseMvpFragment<CommunityLinksPresenter, ICommunityLinksView>
        implements ICommunityLinksView, CommunityLinksAdapter.ActionListener {

    private CommunityLinksAdapter mLinksAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static CommunityLinksFragment newInstance(int accountId, int groupId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        CommunityLinksFragment fragment = new CommunityLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_links, container, false);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(CommunityLinksPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mLinksAdapter = new CommunityLinksAdapter(Collections.emptyList());
        mLinksAdapter.setActionListener(this);

        recyclerView.setAdapter(mLinksAdapter);

        root.findViewById(R.id.button_add).setOnClickListener(v -> callPresenter(CommunityLinksPresenter::fireButtonAddClick));
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<CommunityLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityLinksPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.GROUP_ID),
                saveInstanceState
        );
    }

    @Override
    public void displayRefreshing(boolean loadingNow) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loadingNow);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mLinksAdapter)) {
            mLinksAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayData(List<VKApiCommunity.Link> links) {
        if (nonNull(mLinksAdapter)) {
            mLinksAdapter.setData(links);
        }
    }

    @Override
    public void openLink(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }

    @Override
    public void onClick(VKApiCommunity.Link link) {
        callPresenter(p -> p.fireLinkClick(link));
    }

    @Override
    public void onLongClick(VKApiCommunity.Link link) {
        String[] items = {getString(R.string.edit), getString(R.string.delete)};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(link.name)
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            callPresenter(p -> p.fireLinkEditClick(link));
                            break;
                        case 1:
                            callPresenter(p -> p.fireLinkDeleteClick(link));
                            break;
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}