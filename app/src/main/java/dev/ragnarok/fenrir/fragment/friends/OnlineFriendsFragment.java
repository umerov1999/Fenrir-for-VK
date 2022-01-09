package dev.ragnarok.fenrir.fragment.friends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.OnlineFriendsPresenter;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;

public class OnlineFriendsFragment extends AbsOwnersListFragment<OnlineFriendsPresenter, ISimpleOwnersView> {

    public static OnlineFriendsFragment newInstance(int accountId, int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.USER_ID, userId);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        OnlineFriendsFragment friendsFragment = new OnlineFriendsFragment();
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    @NonNull
    @Override
    public IPresenterFactory<OnlineFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new OnlineFriendsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.USER_ID),
                saveInstanceState);
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