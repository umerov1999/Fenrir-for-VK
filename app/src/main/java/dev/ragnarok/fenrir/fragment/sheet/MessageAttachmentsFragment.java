package dev.ragnarok.fenrir.fragment.sheet;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.AttachmentsActivity;
import dev.ragnarok.fenrir.activity.AudioSelectActivity;
import dev.ragnarok.fenrir.activity.DualTabPhotoActivity;
import dev.ragnarok.fenrir.activity.VideoSelectActivity;
import dev.ragnarok.fenrir.adapter.AttachmentsBottomSheetAdapter;
import dev.ragnarok.fenrir.fragment.FileManagerFragment;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.LocalVideo;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Types;
import dev.ragnarok.fenrir.model.selection.FileManagerSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalGallerySelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalPhotosSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalVideosSelectableSource;
import dev.ragnarok.fenrir.model.selection.Sources;
import dev.ragnarok.fenrir.model.selection.VkPhotosSelectableSource;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.MessageAttachmentsPresenter;
import dev.ragnarok.fenrir.mvp.view.IMessageAttachmentsView;
import dev.ragnarok.fenrir.service.ErrorLocalizer;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import me.minetsh.imaging.IMGEditActivity;

public class MessageAttachmentsFragment extends AbsPresenterBottomSheetFragment<MessageAttachmentsPresenter,
        IMessageAttachmentsView> implements IMessageAttachmentsView, AttachmentsBottomSheetAdapter.ActionListener {
    public static final String MESSAGE_CLOSE_ONLY = "message_attachments_close_only";
    public static final String MESSAGE_SYNC_ATTACHMENTS = "message_attachments_sync";
    private final AppPerms.doRequestPermissions requestCameraPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(MessageAttachmentsPresenter::fireCameraPermissionResolved));
    private final AppPerms.doRequestPermissions requestCameraPermissionScoped = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.CAMERA},
            () -> callPresenter(MessageAttachmentsPresenter::fireCameraPermissionResolved));
    private final ActivityResultLauncher<Uri> openCameraRequest = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
        if (result) {
            callPresenter(MessageAttachmentsPresenter::firePhotoMaked);
        }
    });
    private final ActivityResultLauncher<Intent> openRequestAudioVideoDoc = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            ArrayList<AbsModel> attachments = result.getData().getParcelableArrayListExtra(Extra.ATTACHMENTS);
            callPresenter(p -> p.fireAttachmentsSelected(attachments));
        }
    });
    private final ActivityResultLauncher<Intent> openRequestPhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            ArrayList<Photo> vkphotos = result.getData().getParcelableArrayListExtra(Extra.ATTACHMENTS);
            ArrayList<LocalPhoto> localPhotos = result.getData().getParcelableArrayListExtra(Extra.PHOTOS);
            String file = result.getData().getStringExtra(FileManagerFragment.returnFileParameter);
            LocalVideo video = result.getData().getParcelableExtra(Extra.VIDEO);
            callPresenter(p -> p.firePhotosSelected(vkphotos, localPhotos, file, video));
        }
    });
    private final ActivityResultLauncher<Intent> openRequestResizePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            assert result.getData() != null;
            callPresenter(p -> p.doUploadFile(result.getData().getStringExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH), Upload.IMAGE_SIZE_FULL, false));
        }
    });
    private AttachmentsBottomSheetAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;

    public static MessageAttachmentsFragment newInstance(int accountId, int messageOwnerId, int messageId, ModelsBundle bundle) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.MESSAGE_ID, messageId);
        args.putInt(Extra.OWNER_ID, messageOwnerId);
        args.putParcelable(Extra.BUNDLE, bundle);
        MessageAttachmentsFragment fragment = new MessageAttachmentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (Utils.isLandscape(requireActivity())) {
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity(), getTheme());
            BottomSheetBehavior<?> behavior = dialog.getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
            return dialog;
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(requireActivity(), R.layout.bottom_sheet_attachments, null);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        mEmptyView = view.findViewById(R.id.no_attachments_text);

        view.findViewById(R.id.button_send).setOnClickListener(v -> {
            getParentFragmentManager().setFragmentResult(MESSAGE_CLOSE_ONLY, new Bundle());
            getDialog().dismiss();
        });

        view.findViewById(R.id.button_hide).setOnClickListener(v -> getDialog().dismiss());
        view.findViewById(R.id.button_video).setOnClickListener(v -> callPresenter(MessageAttachmentsPresenter::fireButtonVideoClick));
        view.findViewById(R.id.button_audio).setOnClickListener(v -> callPresenter(MessageAttachmentsPresenter::fireButtonAudioClick));
        view.findViewById(R.id.button_doc).setOnClickListener(v -> callPresenter(MessageAttachmentsPresenter::fireButtonDocClick));
        view.findViewById(R.id.button_camera).setOnClickListener(v -> callPresenter(MessageAttachmentsPresenter::fireButtonCameraClick));
        view.findViewById(R.id.button_photo_settings).setOnClickListener(v -> callPresenter(p -> p.fireCompressSettings(requireActivity())));
        view.findViewById(R.id.button_photo_settings).setVisibility(Settings.get().other().isChange_upload_size() ? View.VISIBLE : View.GONE);

        dialog.setContentView(view);
        fireViewCreated();
    }

    @NonNull
    @Override
    public IPresenterFactory<MessageAttachmentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int messageId = requireArguments().getInt(Extra.MESSAGE_ID);
            int messageOwnerId = requireArguments().getInt(Extra.OWNER_ID);
            ModelsBundle bundle = requireArguments().getParcelable(Extra.BUNDLE);
            return new MessageAttachmentsPresenter(accountId, messageOwnerId, messageId, requireActivity(), bundle, saveInstanceState);
        };
    }

    @Override
    public void displayAttachments(List<AttachmentEntry> entries) {
        if (nonNull(mRecyclerView)) {
            mAdapter = new AttachmentsBottomSheetAdapter(entries, this);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void notifyDataAdded(int positionStart, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(positionStart + 1, count);
        }
    }

    @Override
    public void addPhoto(int accountId, int ownerId) {
        Sources sources = new Sources()
                .with(new LocalPhotosSelectableSource())
                .with(new LocalGallerySelectableSource())
                .with(new LocalVideosSelectableSource())
                .with(new VkPhotosSelectableSource(accountId, ownerId))
                .with(new FileManagerSelectableSource());

        Intent intent = DualTabPhotoActivity.createIntent(requireActivity(), 10, sources);
        openRequestPhoto.launch(intent);
    }

    @Override
    public void notifyEntryRemoved(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(index + 1);
        }
    }

    @Override
    public void displaySelectUploadPhotoSizeDialog(List<LocalPhoto> photos) {
        int[] values = {Upload.IMAGE_SIZE_800, Upload.IMAGE_SIZE_1200, Upload.IMAGE_SIZE_FULL, Upload.IMAGE_SIZE_CROPPING};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j)
                        -> callPresenter(p -> p.fireUploadPhotoSizeSelected(photos, values[j])))
                .setNegativeButton(R.string.button_cancel, null)
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
    public void displaySelectUploadFileSizeDialog(String file) {
        int[] values = {Upload.IMAGE_SIZE_800, Upload.IMAGE_SIZE_1200, Upload.IMAGE_SIZE_FULL, Upload.IMAGE_SIZE_CROPPING};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j)
                        -> callPresenter(p -> p.fireUploadFileSizeSelected(file, values[j])))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void changePercentageSmoothly(int id, int progress) {
        if (nonNull(mAdapter)) {
            mAdapter.changeUploadProgress(id, progress, true);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index + 1);
        }
    }

    @Override
    public void setEmptyViewVisible(boolean visible) {
        if (nonNull(mEmptyView)) {
            mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void requestCameraPermission() {
        if (Utils.hasScopedStorage())
            requestCameraPermissionScoped.launch();
        else
            requestCameraPermission.launch();
    }

    @Override
    public void startCamera(@NonNull Uri fileUri) {
        openCameraRequest.launch(fileUri);
    }

    @Override
    public void syncAccompanyingWithParent(ModelsBundle accompanying) {
        Bundle data = new Bundle();
        data.putParcelable(Extra.BUNDLE, accompanying);
        getParentFragmentManager().setFragmentResult(MESSAGE_SYNC_ATTACHMENTS, data);
    }

    @Override
    public void startAddDocumentActivity(int accountId) {
        Intent intent = AttachmentsActivity.createIntent(requireActivity(), accountId, Types.DOC);
        openRequestAudioVideoDoc.launch(intent);
    }

    @Override
    public void startAddVideoActivity(int accountId, int ownerId) {
        Intent intent = VideoSelectActivity.createIntent(requireActivity(), accountId, ownerId);
        openRequestAudioVideoDoc.launch(intent);
    }

    @Override
    public void startAddAudioActivity(int accountId) {
        Intent intent = AudioSelectActivity.createIntent(requireActivity(), accountId);
        openRequestAudioVideoDoc.launch(intent);
    }

    @Override
    public void onAddPhotoButtonClick() {
        callPresenter(MessageAttachmentsPresenter::fireAddPhotoButtonClick);
    }

    @Override
    public void onButtonRemoveClick(AttachmentEntry entry) {
        callPresenter(p -> p.fireRemoveClick(entry));
    }

    @Override
    public void onButtonRetryClick(AttachmentEntry entry) {
        callPresenter(p -> p.fireRetryClick(entry));
    }

    @Override
    public void showError(String errorText) {
        if (isAdded()) {
            Utils.showRedTopToast(requireActivity(), errorText);
        }
    }

    @Override
    public void showThrowable(Throwable throwable) {
        if (isAdded()) {
            if (getView() == null) {
                showError(ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), throwable));
                return;
            }
            Snackbar.make(getView(), ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), throwable), BaseTransientBottomBar.LENGTH_LONG).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000"))
                    .setAction(R.string.more_info, v -> {
                        StringBuilder Text = new StringBuilder();
                        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                            Text.append("    ");
                            Text.append(stackTraceElement);
                            Text.append("\r\n");
                        }
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setIcon(R.drawable.ic_error)
                                .setMessage(Text)
                                .setTitle(R.string.more_info)
                                .setPositiveButton(R.string.button_ok, null)
                                .setCancelable(true)
                                .show();
                    }).setActionTextColor(Color.WHITE).show();
        }
    }

    @Override
    public void showError(int titleTes, Object... params) {
        if (isAdded()) {
            showError(getString(titleTes, params));
        }
    }

    @Override
    public CustomToast getCustomToast() {
        if (isAdded()) {
            return CustomToast.CreateCustomToast(requireActivity());
        }
        return CustomToast.CreateCustomToast(null);
    }
}