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
import dev.ragnarok.fenrir.adapter.PeopleAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpBottomSheetDialogFragment;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommunityFriendsPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityFriendsView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.view.MySearchView;

public class CommunityFriendsFragment extends BaseMvpBottomSheetDialogFragment<CommunityFriendsPresenter, ICommunityFriendsView>
        implements ICommunityFriendsView, PeopleAdapter.ClickListener {

    private PeopleAdapter mAdapter;

    private static Bundle buildArgs(int accountId, int groupId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        return args;
    }

    public static CommunityFriendsFragment newInstance(int accountId, int groupId) {
        CommunityFriendsFragment fragment = new CommunityFriendsFragment();
        fragment.setArguments(buildArgs(accountId, groupId));
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
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_group_friends, container, false);

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

        mAdapter = new PeopleAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void displayData(List<Owner> users) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(users);
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
    public void displayRefreshing(boolean refreshing) {

    }

    @NonNull
    @Override
    public IPresenterFactory<CommunityFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityFriendsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.GROUP_ID),
                saveInstanceState
        );
    }

    @Override
    public void onOwnerClick(Owner owner) {
        callPresenter(p -> p.fireUserClick(owner));
    }
}
