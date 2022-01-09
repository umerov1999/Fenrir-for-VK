package dev.ragnarok.fenrir.fragment.friends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.MutualFriendsPresenter;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;

public class MutualFriendsFragment extends AbsOwnersListFragment<MutualFriendsPresenter, ISimpleOwnersView> {

    private static final String EXTRA_TARGET_ID = "targetId";

    public static MutualFriendsFragment newInstance(int accountId, int targetId) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TARGET_ID, targetId);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        MutualFriendsFragment friendsFragment = new MutualFriendsFragment();
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    @NonNull
    @Override
    public IPresenterFactory<MutualFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new MutualFriendsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(EXTRA_TARGET_ID),
                saveInstanceState
        );
    }

    @Override
    protected boolean hasToolbar() {
        return false;
    }

    @Override
    protected boolean needShowCount() {
        return false;
    }
}