package dev.ragnarok.fenrir.fragment.friends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.OwnersAdapter;
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FollowersPresenter;
import dev.ragnarok.fenrir.mvp.view.IFollowersView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.Utils;

public class FollowersFragment extends AbsOwnersListFragment<FollowersPresenter, IFollowersView>
        implements IFollowersView {
    public static FollowersFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        FollowersFragment followersFragment = new FollowersFragment();
        followersFragment.setArguments(args);
        return followersFragment;
    }

    @NonNull
    @Override
    public IPresenterFactory<FollowersPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FollowersPresenter(requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.USER_ID),
                saveInstanceState);
    }

    @Override
    public void notifyRemoved(int position) {
        mOwnersAdapter.notifyItemRemoved(position);
    }

    @Override
    protected boolean onLongClick(Owner owner) {
        callPresenter(p -> p.removeFollower(owner));
        return true;
    }

    private void showNotFollowers(@NonNull List<Owner> data, int accountId) {
        OwnersAdapter adapter = new OwnersAdapter(requireActivity(), data);
        adapter.setClickListener(owner -> Utils.openPlaceWithSwipebleActivity(requireActivity(), PlaceFactory.getOwnerWallPlace(accountId, owner.getOwnerId(), null)));
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(requireActivity().getString(R.string.not_follower))
                .setView(Utils.createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
                .setPositiveButton(R.string.button_ok, (dialog, which) -> callPresenter(p -> p.clearModificationFollowers(false, true)))
                .setCancelable(false)
                .show();
    }

    @Override
    public void showModFollowers(@Nullable List<Owner> add, @Nullable List<Owner> remove, int accountId) {
        if (Utils.isEmpty(add) && Utils.isEmpty(remove)) {
            return;
        }
        if (Utils.isEmpty(add) && !Utils.isEmpty(remove)) {
            showNotFollowers(remove, accountId);
            return;
        }
        OwnersAdapter adapter = new OwnersAdapter(requireActivity(), add);
        adapter.setClickListener(owner -> Utils.openPlaceWithSwipebleActivity(requireActivity(), PlaceFactory.getOwnerWallPlace(accountId, owner.getOwnerId(), null)));
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(requireActivity().getString(R.string.new_follower))
                .setView(Utils.createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    callPresenter(p -> p.clearModificationFollowers(true, false));
                    if (!Utils.isEmpty(remove)) {
                        showNotFollowers(remove, accountId);
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected boolean hasToolbar() {
        return false;
    }

    @Override
    protected boolean needShowCount() {
        return true;
    }
}
