package dev.ragnarok.fenrir.fragment.attachments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommentCreatePresenter;
import dev.ragnarok.fenrir.mvp.view.ICreateCommentView;

public class CommentCreateFragment extends AbsAttachmentsEditFragment<CommentCreatePresenter, ICreateCommentView>
        implements ICreateCommentView {

    public static final String REQUEST_CREATE_COMMENT = "request_create_comment";

    public static CommentCreateFragment newInstance(int accountId, int commentDbid, int sourceOwnerId, String body) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.COMMENT_ID, commentDbid);
        args.putInt(Extra.OWNER_ID, sourceOwnerId);
        args.putString(Extra.BODY, body);
        CommentCreateFragment fragment = new CommentCreateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_attchments, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ready) {
            callPresenter(CommentCreatePresenter::fireReadyClick);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public IPresenterFactory<CommentCreatePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int commentDbid = requireArguments().getInt(Extra.COMMENT_ID);
            int sourceOwnerId = requireArguments().getInt(Extra.COMMENT_ID);
            String body = requireArguments().getString(Extra.BODY);
            return new CommentCreatePresenter(accountId, commentDbid, sourceOwnerId, body, saveInstanceState);
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        ActivityUtils.setToolbarTitle(this, R.string.new_comment);
        ActivityUtils.setToolbarSubtitle(this, null);

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }
    }

    @Override
    public boolean onBackPressed() {
        return callPresenter(CommentCreatePresenter::onBackPressed, false);
    }

    @Override
    public void returnDataToParent(String textBody) {
        Bundle data = new Bundle();
        data.putString(Extra.BODY, textBody);

        getParentFragmentManager().setFragmentResult(REQUEST_CREATE_COMMENT, data);
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }
}
