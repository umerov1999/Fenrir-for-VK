package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.GroupChatsAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.model.GroupChats;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.GroupChatsPresenter;
import dev.ragnarok.fenrir.mvp.view.IGroupChatsView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper;

public class GroupChatsFragment extends BaseMvpFragment<GroupChatsPresenter, IGroupChatsView>
        implements SwipeRefreshLayout.OnRefreshListener, IGroupChatsView, GroupChatsAdapter.ActionListener {

    private GroupChatsAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoadMoreFooterHelper helper;

    public static Bundle buildArgs(int accountId, int groupId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        return args;
    }

    public static GroupChatsFragment newInstance(Bundle args) {
        GroupChatsFragment fragment = new GroupChatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static GroupChatsFragment newInstance(int accountId, int ownerId) {
        return newInstance(buildArgs(accountId, ownerId));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group_chats, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity());

        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(GroupChatsPresenter::fireScrollToEnd);
            }
        });

        mAdapter = new GroupChatsAdapter(requireActivity(), Collections.emptyList(), this);

        View footer = inflater.inflate(R.layout.footer_load_more, recyclerView, false);
        helper = LoadMoreFooterHelper.createFrom(footer, () -> callPresenter(GroupChatsPresenter::fireLoadMoreClick));
        mAdapter.addFooter(footer);

        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.group_chats);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onRefresh() {
        callPresenter(GroupChatsPresenter::fireRefresh);
    }

    @Override
    public void displayData(@NonNull List<GroupChats> chats) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(chats);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdd(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void setupLoadMore(@LoadMoreState int state) {
        if (nonNull(helper)) {
            helper.switchToState(state);
        }
    }

    @Override
    public void goToChat(int accountId, int chat_id) {
        PlaceFactory.getChatPlace(accountId, accountId, new Peer(Peer.fromChatId(chat_id))).tryOpenWith(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<GroupChatsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int groupId = requireArguments().getInt(Extra.GROUP_ID);
            return new GroupChatsPresenter(accountId, groupId, saveInstanceState);
        };
    }

    @Override
    public void onGroupChatsClick(@NonNull GroupChats chat) {
        callPresenter(p -> p.fireGroupChatsClick(chat));
    }
}
