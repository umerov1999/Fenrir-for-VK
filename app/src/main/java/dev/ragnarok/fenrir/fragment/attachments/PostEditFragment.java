package dev.ragnarok.fenrir.fragment.attachments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.WallEditorAttrs;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.PostEditPresenter;
import dev.ragnarok.fenrir.mvp.view.IPostEditView;
import dev.ragnarok.fenrir.util.AssertUtils;

public class PostEditFragment extends AbsPostEditFragment<PostEditPresenter, IPostEditView>
        implements IPostEditView {

    public static PostEditFragment newInstance(Bundle args) {
        PostEditFragment fragment = new PostEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle buildArgs(int accountId, @NonNull Post post, @NonNull WallEditorAttrs attrs) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.POST, post);
        args.putParcelable(Extra.ATTRS, attrs);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    public IPresenterFactory<PostEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            Post post = requireArguments().getParcelable(Extra.POST);
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            WallEditorAttrs attrs = requireArguments().getParcelable(Extra.ATTRS);

            AssertUtils.requireNonNull(post);
            AssertUtils.requireNonNull(attrs);
            return new PostEditPresenter(accountId, post, attrs, saveInstanceState);
        };
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_attchments, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ready) {
            callPresenter(PostEditPresenter::fireReadyClick);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void closeAsSuccess() {
        requireActivity().onBackPressed();
    }

    @Override
    public void showConfirmExitDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.save_changes_question)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> callPresenter(PostEditPresenter::fireExitWithSavingConfirmed))
                .setNegativeButton(R.string.button_no, (dialog, which) -> callPresenter(PostEditPresenter::fireExitWithoutSavingClick))
                .setNeutralButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public boolean onBackPressed() {
        return callPresenter(PostEditPresenter::onBackPressed, false);
    }
}
