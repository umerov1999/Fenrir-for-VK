package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.holder.IdentificableHolder;
import dev.ragnarok.fenrir.adapter.holder.SharedHolders;
import dev.ragnarok.fenrir.adapter.multidata.DifferentDataAdapter;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.CircleRoadProgress;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class BigVkPhotosAdapter extends DifferentDataAdapter {

    public static final int DATA_TYPE_PHOTO = 1;
    public static final int DATA_TYPE_UPLOAD = 0;

    private static final String TAG = BigVkPhotosAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_PHOTO = 0;
    private static final int VIEW_TYPE_UPLOAD = 1;
    private static int holderIdGenerator;
    private final SharedHolders<UploadViewHolder> mUploadViewHolders;
    private final Set<PhotoViewHolder> mPhotoHolders;
    private final Context mContext;
    private final int mColorPrimaryWithAlpha;
    private final int mColorSecondaryWithAlpha;
    private final String mPicassoTag;
    private boolean isShowBDate;
    private PhotosActionListener mPhotosActionListener;
    private UploadActionListener mUploadActionListener;

    public BigVkPhotosAdapter(Context context, @NonNull List<Upload> uploads, @NonNull List<SelectablePhotoWrapper> photoWrappers, String picassoTag) {
        mContext = context;
        mPhotoHolders = new HashSet<>();
        mUploadViewHolders = new SharedHolders<>(false);
        mPicassoTag = picassoTag;
        mColorPrimaryWithAlpha = Utils.adjustAlpha(CurrentTheme.getColorPrimary(mContext), 0.75F);
        mColorSecondaryWithAlpha = Utils.adjustAlpha(CurrentTheme.getColorSecondary(mContext), 0.60F);

        setData(DATA_TYPE_UPLOAD, uploads);
        setData(DATA_TYPE_PHOTO, photoWrappers);
    }

    private static int generateNextHolderId() {
        holderIdGenerator++;
        return holderIdGenerator;
    }

    public void setIsShowDate(boolean show) {
        isShowBDate = show;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PHOTO:
                return new PhotoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.vk_photo_item_big, parent, false));
            case VIEW_TYPE_UPLOAD:
                return new UploadViewHolder(LayoutInflater.from(mContext).inflate(R.layout.vk_upload_photo_item, parent, false));
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int adapterPosition) {
        switch (getItemViewType(adapterPosition)) {
            case VIEW_TYPE_PHOTO:
                bindPhotoViewHolder((PhotoViewHolder) holder, getItem(adapterPosition));
                break;
            case VIEW_TYPE_UPLOAD:
                bindUploadViewHolder((UploadViewHolder) holder, getItem(adapterPosition));
                break;
        }
    }

    private void removePhotoViewHolderByTag(@NonNull SelectablePhotoWrapper tag) {
        Iterator<PhotoViewHolder> iterator = mPhotoHolders.iterator();
        while (iterator.hasNext()) {
            if (tag.equals(iterator.next().itemView.getTag())) {
                iterator.remove();
            }
        }
    }

    private void bindUploadViewHolder(UploadViewHolder holder, Upload upload) {
        mUploadViewHolders.put(upload.getId(), holder);

        holder.setupProgress(upload.getStatus(), upload.getProgress(), false);
        holder.setupTitle(upload.getStatus(), upload.getProgress());

        PicassoInstance.with()
                .load(PicassoInstance.buildUriForPicasso(Content_Local.PHOTO, upload.getFileId()))
                .tag(mPicassoTag)
                .placeholder(R.drawable.background_gray)
                .into(holder.image);

        holder.progressRoot.setOnClickListener(v -> {
            if (mUploadActionListener != null) {
                mUploadActionListener.onUploadRemoveClicked(upload);
            }
        });
    }

    private void bindPhotoViewHolder(PhotoViewHolder holder, SelectablePhotoWrapper photoWrapper) {
        removePhotoViewHolderByTag(photoWrapper);
        holder.itemView.setTag(photoWrapper);
        mPhotoHolders.add(holder);
        Logger.d(TAG, "Added photo view holder, total size: " + mPhotoHolders.size());

        Photo photo = photoWrapper.getPhoto();

        holder.tvLike.setText(AppTextUtils.getCounterWithK(photo.getLikesCount()));
        holder.tvLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        holder.ivLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);

        holder.ivDownload.setVisibility(photoWrapper.isDownloaded() ? View.VISIBLE : View.GONE);

        holder.tvComment.setText(AppTextUtils.getCounterWithK(photo.getCommentsCount()));
        holder.tvComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);
        holder.ivComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        holder.bottomRoot.setBackgroundColor(mColorPrimaryWithAlpha);
        holder.bottomRoot.setVisibility(photo.getLikesCount() + photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        if (isShowBDate) {
            holder.bottomTop.setVisibility(View.VISIBLE);
            holder.bottomTop.setBackgroundColor(mColorSecondaryWithAlpha);
            holder.tvDate.setText(AppTextUtils.getDateFromUnixTimeShorted(mContext, photo.getDate()));
        } else {
            holder.bottomTop.setVisibility(View.GONE);
        }

        if (Settings.get().other().isNative_parcel_photo() && FenrirNative.isNativeLoaded()) {
            if (photoWrapper.getCurrent()) {
                holder.current.setVisibility(View.VISIBLE);
                holder.current.fromRes(R.raw.donater_fire, Utils.dp(100), Utils.dp(100), new int[]{0xFF812E, CurrentTheme.getColorPrimary(mContext)}, true);
                holder.current.playAnimation();
            } else {
                holder.current.setVisibility(View.GONE);
                holder.current.clearAnimationDrawable();
            }
        }

        holder.setSelected(photoWrapper.isSelected());
        holder.resolveIndexText(photoWrapper);

        String targetUrl = photo.getUrlForSize(PhotoSize.Q, false);

        if (!Utils.isEmpty(targetUrl)) {
            PicassoInstance.with()
                    .load(targetUrl)
                    .tag(mPicassoTag)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
        }

        View.OnClickListener clickListener = v -> {
            if (mPhotosActionListener != null) {
                mPhotosActionListener.onPhotoClick(holder, photoWrapper);
            }
        };

        holder.photoImageView.setOnClickListener(clickListener);
        holder.index.setOnClickListener(clickListener);
        holder.darkView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemViewType(int adapterPosition) {
        int dataType = getDataTypeByAdapterPosition(adapterPosition);

        switch (dataType) {
            case DATA_TYPE_PHOTO:
                return VIEW_TYPE_PHOTO;
            case DATA_TYPE_UPLOAD:
                return VIEW_TYPE_UPLOAD;
        }

        throw new IllegalStateException("Unknown data type, dataType: " + dataType);
    }

    public void setPhotosActionListener(PhotosActionListener photosActionListener) {
        mPhotosActionListener = photosActionListener;
    }

    public void setUploadActionListener(UploadActionListener uploadActionListener) {
        mUploadActionListener = uploadActionListener;
    }

    public void updatePhotoHoldersSelectionAndIndexes() {
        for (PhotoViewHolder holder : mPhotoHolders) {
            SelectablePhotoWrapper photo = (SelectablePhotoWrapper) holder.itemView.getTag();
            holder.setSelected(photo.isSelected());
            holder.resolveIndexText(photo);
        }
    }

    public void updateUploadHoldersProgress(int uploadId, boolean smoothly, int progress) {
        UploadViewHolder holder = mUploadViewHolders.findOneByEntityId(uploadId);

        if (nonNull(holder)) {
            if (smoothly) {
                holder.progress.changePercentageSmoothly(progress);
            } else {
                holder.progress.changePercentage(progress);
            }

            String progressText = progress + "%";
            holder.title.setText(progressText);
        }
    }

    public void cleanup() {
        mPhotoHolders.clear();
        mUploadViewHolders.release();
    }

    public interface PhotosActionListener {
        void onPhotoClick(PhotoViewHolder holder, SelectablePhotoWrapper photoWrapper);
    }

    public interface UploadActionListener {
        void onUploadRemoveClicked(Upload upload);
    }

    private static class UploadViewHolder extends RecyclerView.ViewHolder implements IdentificableHolder {

        final ImageView image;
        final View progressRoot;
        final CircleRoadProgress progress;
        final TextView title;

        UploadViewHolder(View itemView) {
            super(itemView);
            super.itemView.setTag(generateNextHolderId());

            image = itemView.findViewById(R.id.image);
            progressRoot = itemView.findViewById(R.id.progress_root);
            progress = itemView.findViewById(R.id.progress);
            title = itemView.findViewById(R.id.title);
        }

        void setupProgress(int status, int progressValue, boolean smoothly) {
            if (smoothly && status == Upload.STATUS_UPLOADING) {
                progress.changePercentageSmoothly(progressValue);
            } else {
                progress.setVisibility(status == Upload.STATUS_UPLOADING ? View.VISIBLE : View.GONE);
                progress.changePercentage(status == Upload.STATUS_UPLOADING ? progressValue : 0);
            }
        }

        void setupTitle(int status, int progress) {
            switch (status) {
                case Upload.STATUS_QUEUE:
                    title.setText(R.string.in_order);
                    break;
                case Upload.STATUS_UPLOADING:
                    String progressText = progress + "%";
                    title.setText(progressText);
                    break;
                case Upload.STATUS_ERROR:
                    title.setText(R.string.error);
                    break;
                case Upload.STATUS_CANCELLING:
                    title.setText(R.string.cancelling);
                    break;
            }
        }

        @Override
        public int getHolderId() {
            return (int) itemView.getTag();
        }
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        final ImageView photoImageView;
        final TextView index;
        final View darkView;
        final ViewGroup bottomRoot;
        final ViewGroup bottomTop;
        final TextView tvLike;
        final TextView tvDate;
        final TextView tvComment;
        final ImageView ivLike;
        final ImageView ivComment;
        final ImageView ivDownload;
        final RLottieImageView current;

        PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.imageView);
            index = itemView.findViewById(R.id.item_photo_index);
            darkView = itemView.findViewById(R.id.selected);
            bottomRoot = itemView.findViewById(R.id.vk_photo_item_bottom);
            bottomTop = itemView.findViewById(R.id.vk_photo_item_top);
            ivLike = itemView.findViewById(R.id.vk_photo_item_like);
            tvLike = itemView.findViewById(R.id.vk_photo_item_like_counter);
            ivComment = itemView.findViewById(R.id.vk_photo_item_comment);
            tvComment = itemView.findViewById(R.id.vk_photo_item_comment_counter);
            ivDownload = itemView.findViewById(R.id.is_downloaded);
            tvDate = itemView.findViewById(R.id.vk_photo_item_date);
            current = itemView.findViewById(R.id.current);
        }

        public void setSelected(boolean selected) {
            index.setVisibility(selected ? View.VISIBLE : View.GONE);
            darkView.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        void resolveIndexText(SelectablePhotoWrapper photo) {
            index.setText(photo.getIndex() == 0 ? "" : String.valueOf(photo.getIndex()));
        }
    }
}
