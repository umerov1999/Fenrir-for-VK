package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.LikesListPresenter;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;

public class LikesFragment extends AbsOwnersListFragment<LikesListPresenter, ISimpleOwnersView> {

    public static Bundle buildArgs(int accountId, String type, int ownerId, int itemId, String filter) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putString(Extra.TYPE, type);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ITEM_ID, itemId);
        args.putString(Extra.FILTER, filter);
        return args;
    }

    public static LikesFragment newInstance(@NonNull Bundle args) {
        LikesFragment fragment = new LikesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle("likes".equals(requireArguments().getString(Extra.FILTER)) ? R.string.like : R.string.shared);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<LikesListPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LikesListPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getString(Extra.TYPE),
                requireArguments().getInt(Extra.OWNER_ID),
                requireArguments().getInt(Extra.ITEM_ID),
                requireArguments().getString(Extra.FILTER),
                saveInstanceState
        );
    }

    @Override
    protected boolean hasToolbar() {
        return true;
    }

    @Override
    protected boolean needShowCount() {
        return false;
    }
}
