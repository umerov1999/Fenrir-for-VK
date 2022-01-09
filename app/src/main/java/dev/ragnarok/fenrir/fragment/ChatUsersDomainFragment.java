package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.ChatMembersListDomainAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpBottomSheetDialogFragment;
import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.ChatUsersDomainPresenter;
import dev.ragnarok.fenrir.mvp.view.IChatUsersDomainView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.view.MySearchView;

public class ChatUsersDomainFragment extends BaseMvpBottomSheetDialogFragment<ChatUsersDomainPresenter, IChatUsersDomainView>
        implements IChatUsersDomainView, ChatMembersListDomainAdapter.ActionListener {

    private ChatMembersListDomainAdapter mAdapter;
    private Listener listener;

    private static Bundle buildArgs(int accountId, int chatId) {
        Bundle args = new Bundle();
        args.putInt(Extra.CHAT_ID, chatId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static ChatUsersDomainFragment newInstance(int accountId, int chatId, Listener listener) {
        ChatUsersDomainFragment fragment = new ChatUsersDomainFragment();
        fragment.listener = listener;
        fragment.setArguments(buildArgs(accountId, chatId));
        return fragment;
    }

    @Override
    @NonNull
    public BottomSheetDialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity(), getTheme());
        BottomSheetBehavior<FrameLayout> behavior = dialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        behavior.setSkipCollapsed(true);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat_users_domain, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callPresenter(p -> p.fireQuery(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callPresenter(p -> p.fireQuery(newText));
                return false;
            }
        });

        mAdapter = new ChatMembersListDomainAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setActionListener(this);
        recyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void displayData(List<AppChatUser> users) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(users);
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void openUserWall(int accountId, Owner user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity());
    }

    @Override
    public void addDomain(int accountId, Owner user) {
        if (nonNull(listener)) {
            listener.onSelected(user);
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {

    }

    @NonNull
    @Override
    public IPresenterFactory<ChatUsersDomainPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatUsersDomainPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.CHAT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onUserClick(AppChatUser user) {
        callPresenter(p -> p.fireUserClick(user));
    }

    @Override
    public boolean onUserLongClick(AppChatUser user) {
        callPresenter(p -> p.fireUserLongClick(user));
        return true;
    }

    public interface Listener {
        void onSelected(Owner user);
    }
}
