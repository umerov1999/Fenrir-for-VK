package dev.ragnarok.fenrir.fragment.attachments;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.dialog.ImageSizeAlertDialog;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.model.WallEditorAttrs;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.PostCreatePresenter;
import dev.ragnarok.fenrir.mvp.view.IPostCreateView;
import dev.ragnarok.fenrir.util.AssertUtils;

public class PostCreateFragment extends AbsPostEditFragment<PostCreatePresenter, IPostCreateView>
        implements IPostCreateView {

    private static final String EXTRA_EDITING_TYPE = "editing_type";
    private static final String EXTRA_STREAMS = "streams";

    public static PostCreateFragment newInstance(Bundle args) {
        PostCreateFragment fragment = new PostCreateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle buildArgs(int accountId, int ownerId, @EditingPostType int editingType,
                                   ModelsBundle bundle, @NonNull WallEditorAttrs attrs,
                                   @Nullable ArrayList<Uri> streams, @Nullable String body, @Nullable String mime) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_EDITING_TYPE, editingType);
        args.putParcelableArrayList(EXTRA_STREAMS, streams);

        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putString(Extra.BODY, body);
        args.putParcelable(Extra.BUNDLE, bundle);
        args.putParcelable(Extra.ATTRS, attrs);
        args.putString(Extra.TYPE, mime);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    public IPresenterFactory<PostCreatePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            @EditingPostType
            int type = requireArguments().getInt(EXTRA_EDITING_TYPE);

            ModelsBundle bundle = requireArguments().getParcelable(Extra.BUNDLE);

            WallEditorAttrs attrs = requireArguments().getParcelable(Extra.ATTRS);
            AssertUtils.requireNonNull(attrs);

            String links = requireArguments().getString(Extra.BODY);
            String mime = requireArguments().getString(Extra.TYPE);

            ArrayList<Uri> streams = requireArguments().getParcelableArrayList(EXTRA_STREAMS);
            requireArguments().remove(EXTRA_STREAMS); // only first start
            requireArguments().remove(Extra.BODY);
            return new PostCreatePresenter(accountId, ownerId, type, bundle, attrs, streams, links, mime, saveInstanceState);
        };
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
    public void displayUploadUriSizeDialog(@NonNull List<Uri> uris) {
        new ImageSizeAlertDialog.Builder(requireActivity())
                .setOnSelectedCallback(size -> callPresenter(p -> p.fireUriUploadSizeSelected(uris, size)))
                .setOnCancelCallback(() -> callPresenter(PostCreatePresenter::fireUriUploadCancelClick))
                .show();
    }

    @Override
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
            callPresenter(PostCreatePresenter::fireReadyClick);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        return callPresenter(PostCreatePresenter::onBackPresed, false);
    }
}
