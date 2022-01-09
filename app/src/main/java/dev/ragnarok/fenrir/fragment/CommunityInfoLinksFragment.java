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

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.CommunityInfoLinksAdapter;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommunityInfoLinksPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityInfoLinksView;
import dev.ragnarok.fenrir.util.ViewUtils;

public class CommunityInfoLinksFragment extends BaseMvpFragment<CommunityInfoLinksPresenter, ICommunityInfoLinksView>
        implements ICommunityInfoLinksView, CommunityInfoLinksAdapter.ActionListener {

    private CommunityInfoLinksAdapter mLinksAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static CommunityInfoLinksFragment newInstance(int accountId, Community groupId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.GROUP_ID, groupId);
        CommunityInfoLinksFragment fragment = new CommunityInfoLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_links, container, false);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(CommunityInfoLinksPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mLinksAdapter = new CommunityInfoLinksAdapter(Collections.emptyList());
        mLinksAdapter.setActionListener(this);

        recyclerView.setAdapter(mLinksAdapter);

        root.findViewById(R.id.button_add).setVisibility(View.INVISIBLE);
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<CommunityInfoLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityInfoLinksPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.GROUP_ID),
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
}
