package dev.ragnarok.fenrir.fragment.attachments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.RepostPresenter;
import dev.ragnarok.fenrir.mvp.view.IRepostView;

public class RepostFragment extends AbsAttachmentsEditFragment<RepostPresenter, IRepostView> implements IRepostView {

    private static final String EXTRA_POST = "post";
    private static final String EXTRA_GROUP_ID = "group_id";

    public static RepostFragment newInstance(Bundle args) {
        RepostFragment fragment = new RepostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static RepostFragment newInstance(int accountId, Integer gid, Post post) {
        RepostFragment fragment = new RepostFragment();
        fragment.setArguments(buildArgs(accountId, gid, post));
        return fragment;
    }

    public static Bundle buildArgs(int accountId, Integer groupId, Post post) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_POST, post);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        if (groupId != null) {
            bundle.putInt(EXTRA_GROUP_ID, groupId);
        }

        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void goBack() {
        requireActivity().onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_attchments, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ready) {
            callPresenter(RepostPresenter::fireReadyClick);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.share);
            actionBar.setSubtitle(null);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<RepostPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            Post post = requireArguments().getParcelable(EXTRA_POST);
            Integer groupId = requireArguments().containsKey(EXTRA_GROUP_ID) ? requireArguments().getInt(EXTRA_GROUP_ID) : null;
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            return new RepostPresenter(accountId, post, groupId, saveInstanceState);
        };
    }
}
