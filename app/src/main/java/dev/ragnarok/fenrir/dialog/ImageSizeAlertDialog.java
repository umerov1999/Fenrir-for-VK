package dev.ragnarok.fenrir.dialog;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.Objects;

public class ImageSizeAlertDialog {

    private final Activity mActivity;
    private final OnSelectedCallback mOnSelectedCallback;
    private final OnCancelCallback mOnCancelCallback;

    private ImageSizeAlertDialog(ImageSizeAlertDialog.Builder builder) {
        mActivity = builder.mActivity;
        mOnCancelCallback = builder.mOnCancelCallback;
        mOnSelectedCallback = builder.mOnSelectedCallback;
    }

    public static void showUploadPhotoSizeIfNeed(Activity activity, Callback callback) {
        Integer size = Settings.get()
                .main()
                .getUploadImageSize();

        if (Objects.isNull(size)) {
            AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.select_image_size_title))
                    .setItems(R.array.array_image_sizes_names, (dialogInterface, j) -> {
                        int selectedSize = Upload.IMAGE_SIZE_FULL;

                        switch (j) {
                            case 0:
                                selectedSize = Upload.IMAGE_SIZE_800;
                                break;
                            case 1:
                                selectedSize = Upload.IMAGE_SIZE_1200;
                                break;
                            case 2:
                                selectedSize = Upload.IMAGE_SIZE_FULL;
                                break;
                            case 3:
                                selectedSize = Upload.IMAGE_SIZE_CROPPING;
                                break;
                        }

                        callback.onSizeSelected(selectedSize);
                    }).setCancelable(true).create();
            dialog.show();
        } else {
            callback.onSizeSelected(size);
        }
    }

    public void show() {
        new MaterialAlertDialogBuilder(mActivity)
                .setTitle(mActivity.getString(R.string.select_image_size_title))
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j) -> {
                    int selectedSize = Upload.IMAGE_SIZE_FULL;
                    switch (j) {
                        case 0:
                            selectedSize = Upload.IMAGE_SIZE_800;
                            break;
                        case 1:
                            selectedSize = Upload.IMAGE_SIZE_1200;
                            break;
                        case 2:
                            selectedSize = Upload.IMAGE_SIZE_FULL;
                            break;
                        case 3:
                            selectedSize = Upload.IMAGE_SIZE_CROPPING;
                            break;
                    }

                    if (Objects.nonNull(mOnSelectedCallback)) {
                        mOnSelectedCallback.onSizeSelected(selectedSize);
                    }
                })
                .setCancelable(false)
                .setNegativeButton(R.string.button_cancel, (dialog1, which) -> {
                    if (Objects.nonNull(mOnCancelCallback)) {
                        mOnCancelCallback.onCancel();
                    }
                })
                .show();
    }

    public interface OnSelectedCallback {
        void onSizeSelected(int size);
    }

    public interface OnCancelCallback {
        void onCancel();
    }

    public interface Callback {
        void onSizeSelected(int size);
    }

    public static class Builder {

        private final Activity mActivity;
        private OnSelectedCallback mOnSelectedCallback;
        private OnCancelCallback mOnCancelCallback;

        public Builder(Activity activity) {
            mActivity = activity;
        }

        public Builder setOnSelectedCallback(OnSelectedCallback onSelectedCallback) {
            mOnSelectedCallback = onSelectedCallback;
            return this;
        }

        public Builder setOnCancelCallback(OnCancelCallback onCancelCallback) {
            mOnCancelCallback = onCancelCallback;
            return this;
        }

        public ImageSizeAlertDialog build() {
            return new ImageSizeAlertDialog(this);
        }

        public void show() {
            build().show();
        }
    }
}
