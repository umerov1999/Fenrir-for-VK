package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoAlbumEditor;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.EditPhotoAlbumPresenter;
import dev.ragnarok.fenrir.mvp.view.IEditPhotoAlbumView;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.view.steppers.base.AbsStepHolder;
import dev.ragnarok.fenrir.view.steppers.base.AbsSteppersVerticalAdapter;
import dev.ragnarok.fenrir.view.steppers.impl.CreatePhotoAlbumStep1Holder;
import dev.ragnarok.fenrir.view.steppers.impl.CreatePhotoAlbumStep2Holder;
import dev.ragnarok.fenrir.view.steppers.impl.CreatePhotoAlbumStep3Holder;
import dev.ragnarok.fenrir.view.steppers.impl.CreatePhotoAlbumStep4Holder;
import dev.ragnarok.fenrir.view.steppers.impl.CreatePhotoAlbumStepsHost;

public class CreatePhotoAlbumFragment extends BaseMvpFragment<EditPhotoAlbumPresenter, IEditPhotoAlbumView>
        implements BackPressCallback, IEditPhotoAlbumView, CreatePhotoAlbumStep4Holder.ActionListener, CreatePhotoAlbumStep3Holder.ActionListener, CreatePhotoAlbumStep2Holder.ActionListener, CreatePhotoAlbumStep1Holder.ActionListener {

    private static final String EXTRA_EDITOR = "editor";

    private RecyclerView mRecyclerView;
    private AbsSteppersVerticalAdapter<CreatePhotoAlbumStepsHost> mAdapter;

    public static Bundle buildArgsForEdit(int aid, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_EDITOR, editor);
        bundle.putParcelable(Extra.ALBUM, album);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static Bundle buildArgsForCreate(int aid, int ownerId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.OWNER_ID, ownerId);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static CreatePhotoAlbumFragment newInstance(Bundle args) {
        CreatePhotoAlbumFragment fragment = new CreatePhotoAlbumFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_photo_album, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mRecyclerView = root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        return root;
    }

    @Override
    public void updateStepView(int step) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(step);
        }
    }

    @Override
    public void moveSteppers(int old, int current) {
        if (Objects.nonNull(mRecyclerView) && Objects.nonNull(mAdapter)) {
            mRecyclerView.scrollToPosition(current);
            mAdapter.notifyItemChanged(old);
            mAdapter.notifyItemChanged(current);
        }
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }

    @Override
    public void hideKeyboard() {
        ActivityUtils.hideSoftKeyboard(requireActivity());
    }

    @Override
    public void updateStepButtonsAvailability(int step) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.updateNextButtonAvailability(step);
        }
    }

    @Override
    public void attachSteppersHost(@NonNull CreatePhotoAlbumStepsHost host) {
        mAdapter = new AbsSteppersVerticalAdapter<CreatePhotoAlbumStepsHost>(host, this) {
            @Override
            public AbsStepHolder<CreatePhotoAlbumStepsHost> createHolderForStep(ViewGroup parent, CreatePhotoAlbumStepsHost host, int step) {
                return createHolder(step, parent);
            }
        };

        mRecyclerView.setAdapter(mAdapter);
    }

    private AbsStepHolder<CreatePhotoAlbumStepsHost> createHolder(int step, ViewGroup parent) {
        switch (step) {
            case CreatePhotoAlbumStepsHost.STEP_TITLE_AND_DESCRIPTION:
                return new CreatePhotoAlbumStep1Holder(parent, this);
            case CreatePhotoAlbumStepsHost.STEP_UPLOAD_AND_COMMENTS:
                return new CreatePhotoAlbumStep2Holder(parent, this);
            case CreatePhotoAlbumStepsHost.STEP_PRIVACY_VIEW:
                return new CreatePhotoAlbumStep3Holder(parent, this);
            case CreatePhotoAlbumStepsHost.STEP_PRIVACY_COMMENT:
                return new CreatePhotoAlbumStep4Holder(parent, this);

            default:
                throw new IllegalArgumentException("Inavalid step index: " + step);
        }
    }

    @Override
    public boolean onBackPressed() {
        return callPresenter(EditPhotoAlbumPresenter::fireBackButtonClick, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.create_album);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onNextButtonClick(int step) {
        callPresenter(p -> p.fireStepPositiveButtonClick(step));
    }

    @Override
    public void onCancelButtonClick(int step) {
        callPresenter(p -> p.fireStepNegativeButtonClick(step));
    }

    @NonNull
    @Override
    public IPresenterFactory<EditPhotoAlbumPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);

            if (requireArguments().containsKey(Extra.ALBUM)) {
                PhotoAlbum abum = requireArguments().getParcelable(Extra.ALBUM);
                PhotoAlbumEditor editor = requireArguments().getParcelable(EXTRA_EDITOR);
                AssertUtils.requireNonNull(abum);
                AssertUtils.requireNonNull(editor);
                return new EditPhotoAlbumPresenter(accountId, abum, editor, requireActivity(), saveInstanceState);
            } else {
                int ownerId = requireArguments().getInt(Extra.OWNER_ID);
                return new EditPhotoAlbumPresenter(accountId, ownerId, requireActivity(), saveInstanceState);
            }
        };
    }

    @Override
    public void onPrivacyCommentClick() {
        callPresenter(EditPhotoAlbumPresenter::firePrivacyCommentClick);
    }

    @Override
    public void onPrivacyViewClick() {
        callPresenter(EditPhotoAlbumPresenter::firePrivacyViewClick);
    }

    @Override
    public void onUploadByAdminsOnlyChecked(boolean checked) {
        callPresenter(p -> p.fireUploadByAdminsOnlyChecked(checked));
    }

    @Override
    public void onCommentsDisableChecked(boolean checked) {
        callPresenter(p -> p.fireDisableCommentsClick(checked));
    }

    @Override
    public void onTitleEdited(CharSequence text) {
        callPresenter(p -> p.fireTitleEdit(text));
    }

    @Override
    public void onDescriptionEdited(CharSequence text) {

    }
}