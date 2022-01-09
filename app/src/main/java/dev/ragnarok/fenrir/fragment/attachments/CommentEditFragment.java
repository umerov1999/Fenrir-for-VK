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
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommentEditPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommentEditView;
import dev.ragnarok.fenrir.util.AssertUtils;

public class CommentEditFragment extends AbsAttachmentsEditFragment<CommentEditPresenter, ICommentEditView>
        implements ICommentEditView {

    public static final String REQUEST_COMMENT_EDIT = "request_comment_edit";

    public static CommentEditFragment newInstance(int accountId, Comment comment, Integer CommentThread) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.COMMENT, comment);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.COMMENT_ID, CommentThread);
        CommentEditFragment fragment = new CommentEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    public IPresenterFactory<CommentEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            Integer CommentThread = requireArguments().getInt(Extra.COMMENT_ID);
            Comment comment = requireArguments().getParcelable(Extra.COMMENT);
            AssertUtils.requireNonNull(comment);
            return new CommentEditPresenter(comment, aid, CommentThread, saveInstanceState);
        };
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_attchments, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ready) {
            callPresenter(CommentEditPresenter::fireReadyClick);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActivityUtils.setToolbarTitle(this, R.string.comment_editing_title);
        ActivityUtils.setToolbarSubtitle(this, null);

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public boolean onBackPressed() {
        return callPresenter(CommentEditPresenter::onBackPressed, false);
    }

    @Override
    public void goBackWithResult(@Nullable Comment comment) {
        Bundle data = new Bundle();
        data.putParcelable(Extra.COMMENT, comment);

        getParentFragmentManager().setFragmentResult(REQUEST_COMMENT_EDIT, data);

        requireActivity().onBackPressed();
    }

    @Override
    public void showConfirmWithoutSavingDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.save_changes_question)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> callPresenter(CommentEditPresenter::fireReadyClick))
                .setNegativeButton(R.string.button_no, (dialog, which) -> callPresenter(CommentEditPresenter::fireSavingCancelClick))
                .setNeutralButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }
}
