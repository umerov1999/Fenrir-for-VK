package dev.ragnarok.fenrir.fragment.attachments;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.AttachmentsActivity;
import dev.ragnarok.fenrir.activity.AudioSelectActivity;
import dev.ragnarok.fenrir.activity.PhotoAlbumsActivity;
import dev.ragnarok.fenrir.activity.PhotosActivity;
import dev.ragnarok.fenrir.adapter.AttchmentsEditorAdapter;
import dev.ragnarok.fenrir.fragment.CreatePollFragment;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Types;
import dev.ragnarok.fenrir.mvp.presenter.AbsAttachmentsEditPresenter;
import dev.ragnarok.fenrir.mvp.view.IBaseAttachmentsEditView;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.Action;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.DateTimePicker;
import dev.ragnarok.fenrir.view.WeakRunnable;
import dev.ragnarok.fenrir.view.YoutubeButton;
import me.minetsh.imaging.IMGEditActivity;

public abstract class AbsAttachmentsEditFragment<P extends AbsAttachmentsEditPresenter<V>, V extends IBaseAttachmentsEditView>
        extends BaseMvpFragment<P, V> implements IBaseAttachmentsEditView, AttchmentsEditorAdapter.Callback, BackPressCallback {

    private final AppPerms.doRequestPermissions requestCameraPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(AbsAttachmentsEditPresenter::fireCameraPermissionResolved));
    private final AppPerms.doRequestPermissions requestCameraPermissionScoped = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.CAMERA},
            () -> callPresenter(AbsAttachmentsEditPresenter::fireCameraPermissionResolved));

    private final AppPerms.doRequestPermissions requestReqadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(AbsAttachmentsEditPresenter::fireReadStoragePermissionResolved));
    private final ActivityResultLauncher<Uri> openCameraRequest = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
        if (result) {
            callPresenter(AbsAttachmentsEditPresenter::firePhotoMaked);
        }
    });
    private final ActivityResultLauncher<Intent> openRequestAudioVideoDoc = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            ArrayList<AbsModel> attachments = result.getData().getParcelableArrayListExtra(Extra.ATTACHMENTS);
            callPresenter(p -> p.fireAttachmentsSelected(attachments));
        }
    });
    private final ActivityResultLauncher<Intent> openRequestPhotoFromVK = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            ArrayList<Photo> photos = result.getData().getParcelableArrayListExtra("attachments");
            AssertUtils.requireNonNull(photos);
            callPresenter(p -> p.fireVkPhotosSelected(photos));
        }
    });
    private final ActivityResultLauncher<Intent> openRequestPhotoFromGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            ArrayList<LocalPhoto> photos = result.getData().getParcelableArrayListExtra(Extra.PHOTOS);
            AssertUtils.requireNonNull(photos);
            callPresenter(p -> p.firePhotosFromGallerySelected(photos));
        }
    });
    private final ActivityResultLauncher<Intent> openRequestResizePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            assert result.getData() != null;
            callPresenter(p -> p.fireFileSelected(result.getData().getStringExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH)));
        }
    });
    private TextInputEditText mTextBody;
    private View mTimerRoot;
    private TextView mTimerTextInfo;
    private View mTimerInfoRoot;
    private View mButtonsBar;
    private YoutubeButton mButtonPhoto;
    private YoutubeButton mButtonAudio;
    private YoutubeButton mButtonVideo;
    private YoutubeButton mButtonDoc;
    private YoutubeButton mButtonPoll;
    private MaterialButton mButtonTimer;
    private AttchmentsEditorAdapter mAdapter;
    private ViewGroup mUnderBodyContainer;
    private TextView mEmptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_attachments_manager_new, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        int spancount = getResources().getInteger(R.integer.attachments_editor_column_count);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager manager = new GridLayoutManager(requireActivity(), spancount);
        recyclerView.setLayoutManager(manager);

        View headerView = inflater.inflate(R.layout.header_attachments_manager, recyclerView, false);

        mAdapter = new AttchmentsEditorAdapter(requireActivity(), Collections.emptyList(), this);
        mAdapter.addHeader(headerView);

        recyclerView.setAdapter(mAdapter);

        mUnderBodyContainer = headerView.findViewById(R.id.under_body_container);

        mTextBody = headerView.findViewById(R.id.fragment_create_post_text);
        mTextBody.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireTextChanged(s));
            }
        });

        mTimerRoot = headerView.findViewById(R.id.timer_root);
        mTimerInfoRoot = headerView.findViewById(R.id.post_schedule_info_root);
        mTimerTextInfo = headerView.findViewById(R.id.post_schedule_info);

        mButtonsBar = headerView.findViewById(R.id.buttons_bar);

        mButtonPhoto = mButtonsBar.findViewById(R.id.fragment_create_post_photo);
        mButtonAudio = mButtonsBar.findViewById(R.id.fragment_create_post_audio);
        mButtonVideo = mButtonsBar.findViewById(R.id.fragment_create_post_video);
        mButtonDoc = mButtonsBar.findViewById(R.id.fragment_create_post_doc);
        mButtonPoll = mButtonsBar.findViewById(R.id.fragment_create_post_poll);

        mButtonTimer = headerView.findViewById(R.id.button_postpone);

        mButtonPhoto.setOnClickListener(view -> callPresenter(AbsAttachmentsEditPresenter::fireButtonPhotoClick));
        mButtonAudio.setOnClickListener(view -> callPresenter(AbsAttachmentsEditPresenter::fireButtonAudioClick));
        mButtonVideo.setOnClickListener(view -> callPresenter(AbsAttachmentsEditPresenter::fireButtonVideoClick));
        mButtonDoc.setOnClickListener(view -> callPresenter(AbsAttachmentsEditPresenter::fireButtonDocClick));
        mButtonPoll.setOnClickListener(view -> callPresenter(AbsAttachmentsEditPresenter::fireButtonPollClick));

        headerView.findViewById(R.id.button_disable_postpone).setOnClickListener(v -> callPresenter(AbsAttachmentsEditPresenter::fireButtonTimerClick));
        mButtonTimer.setOnClickListener(view -> callPresenter(AbsAttachmentsEditPresenter::fireButtonTimerClick));

        mEmptyText = headerView.findViewById(R.id.empty_text);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.cleanup();
    }

    ViewGroup getUnderBodyContainer() {
        return mUnderBodyContainer;
    }

    @Override
    public void updateProgressAtIndex(int attachmentId, int progress) {
        if (nonNull(mAdapter)) {
            mAdapter.updateEntityProgress(attachmentId, progress);
        }
    }

    @Override
    public void displayInitialModels(@NonNull List<AttachmentEntry> models) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(models);
        }

        resolveEmptyTextVisibility();
    }

    @Override
    public void setSupportedButtons(boolean photo, boolean audio, boolean video, boolean doc,
                                    boolean poll, boolean timer) {
        if (nonNull(mButtonPhoto)) {
            mButtonPhoto.setVisibility(photo ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonAudio)) {
            mButtonAudio.setVisibility(audio ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonVideo)) {
            mButtonVideo.setVisibility(video ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonDoc)) {
            mButtonDoc.setVisibility(doc ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonPoll)) {
            mButtonPoll.setVisibility(poll ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mTimerRoot)) {
            mTimerRoot.setVisibility(timer ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonsBar)) {
            mButtonsBar.setVisibility(photo || video || doc || poll ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setTextBody(CharSequence text) {
        if (nonNull(mTextBody)) {
            mTextBody.setText(text);
        }
    }

    @Override
    public void openAddVkPhotosWindow(int maxSelectionCount, int accountId, int ownerId) {
        Intent intent = new Intent(requireActivity(), PhotoAlbumsActivity.class);
        intent.putExtra(Extra.OWNER_ID, accountId);
        intent.putExtra(Extra.ACCOUNT_ID, ownerId);
        intent.putExtra(Extra.ACTION, IVkPhotosView.ACTION_SELECT_PHOTOS);
        openRequestPhotoFromVK.launch(intent);
    }

    private void startAttachmentsActivity(int accountId, int type) {
        Intent intent = new Intent(requireActivity(), AttachmentsActivity.class);
        intent.putExtra(Extra.TYPE, type);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        openRequestAudioVideoDoc.launch(intent);
    }

    @Override
    public void openAddAudiosWindow(int maxSelectionCount, int accountId) {
        Intent intent = AudioSelectActivity.createIntent(requireActivity(), accountId);
        openRequestAudioVideoDoc.launch(intent);
    }

    @Override
    public void openAddVideosWindow(int maxSelectionCount, int accountId) {
        startAttachmentsActivity(accountId, Types.VIDEO);
    }

    @Override
    public void openAddDocumentsWindow(int maxSelectionCount, int accountId) {
        startAttachmentsActivity(accountId, Types.DOC);
    }

    @Override
    public void openAddPhotoFromGalleryWindow(int maxSelectionCount) {
        Intent attachPhotoIntent = new Intent(requireActivity(), PhotosActivity.class);
        attachPhotoIntent.putExtra(PhotosActivity.EXTRA_MAX_SELECTION_COUNT, maxSelectionCount);
        openRequestPhotoFromGallery.launch(attachPhotoIntent);
    }

    @Override
    public void onRemoveClick(int index, @NonNull AttachmentEntry attachment) {
        callPresenter(p -> p.fireRemoveClick(index, attachment));
    }

    @Override
    public void onTitleClick(int index, @NonNull AttachmentEntry attachment) {
        callPresenter(p -> p.fireTitleClick(index, attachment));
    }

    @Override
    public void requestCameraPermission() {
        if (Utils.hasScopedStorage())
            requestCameraPermissionScoped.launch();
        else
            requestCameraPermission.launch();
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestReqadPermission.launch();
    }

    @Override
    public void openCamera(@NonNull Uri photoCameraUri) {
        openCameraRequest.launch(photoCameraUri);
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyItemRangeInsert(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position + mAdapter.getHeadersCount(), count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displaySelectUploadPhotoSizeDialog(@NonNull List<LocalPhoto> photos) {
        int[] values = {Upload.IMAGE_SIZE_800, Upload.IMAGE_SIZE_1200, Upload.IMAGE_SIZE_FULL, Upload.IMAGE_SIZE_CROPPING};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names,
                        (dialogInterface, index) -> callPresenter(p -> p.fireUploadPhotoSizeSelected(photos, values[index])))
                .show();
    }

    @Override
    public void displayCropPhotoDialog(Uri uri) {
        try {
            openRequestResizePhoto.launch(new Intent(requireContext(), IMGEditActivity.class)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, new File(requireActivity().getExternalCacheDir() + File.separator + "scale.jpg").getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openPollCreationWindow(int accountId, int ownerId) {
        PlaceFactory.getCreatePollPlace(accountId, ownerId)
                .setFragmentListener(CreatePollFragment.REQUEST_CREATE_POLL, (requestKey, result) -> {
                    Poll poll = result.getParcelable("poll");
                    AssertUtils.requireNonNull(poll);
                    callPresenter(p -> p.firePollCreated(poll));
                })
                .tryOpenWith(requireActivity());
    }

    @Override
    public void displayChoosePhotoTypeDialog() {
        String[] items = {getString(R.string.from_vk_albums), getString(R.string.from_local_albums), getString(R.string.from_camera)};
        new MaterialAlertDialogBuilder(requireActivity()).setItems(items, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    callPresenter(AbsAttachmentsEditPresenter::firePhotoFromVkChoose);
                    break;
                case 1:
                    callPresenter(AbsAttachmentsEditPresenter::firePhotoFromLocalGalleryChoose);
                    break;
                case 2:
                    callPresenter(AbsAttachmentsEditPresenter::firePhotoFromCameraChoose);
                    break;
            }
        }).show();
    }

    @Override
    public void notifySystemAboutNewPhoto(@NonNull Uri uri) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        requireActivity().sendBroadcast(scanIntent);
    }

    @Override
    public void setTimerValue(Long unixtime) {
        if (nonNull(mButtonTimer)) {
            mButtonTimer.setVisibility(isNull(unixtime) ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mTimerInfoRoot)) {
            mTimerInfoRoot.setVisibility(isNull(unixtime) ? View.GONE : View.VISIBLE);
        }

        if (nonNull(mTimerTextInfo)) {
            if (nonNull(unixtime)) {
                String formattedTime = AppTextUtils.getDateFromUnixTime(requireActivity(), unixtime);
                mTimerTextInfo.setText(getString(R.string.will_be_posted_at, formattedTime.toLowerCase()));
                mTimerTextInfo.setVisibility(View.VISIBLE);
            } else {
                mTimerTextInfo.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(position + mAdapter.getHeadersCount());

            if (mAdapter.getRealItemCount() == 0) {
                postResolveEmptyTextVisibility();
            }
        }
    }

    private void postResolveEmptyTextVisibility() {
        if (nonNull(mEmptyText)) {
            Action<AbsAttachmentsEditFragment<P, V>> action = AbsAttachmentsEditFragment::resolveEmptyTextVisibility;
            mEmptyText.postDelayed(new WeakRunnable<>(this, action), 1000);
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmptyText)) {
            mEmptyText.setVisibility(mAdapter.getRealItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
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
    public void showEnterTimeDialog(long initialTimeUnixtime) {
        new DateTimePicker.Builder(requireActivity())
                .setTime(initialTimeUnixtime)
                .setCallback(unixtime -> callPresenter(p -> p.fireTimerTimeSelected(unixtime)))
                .show();
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}