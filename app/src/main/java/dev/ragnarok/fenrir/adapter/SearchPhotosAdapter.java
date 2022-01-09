package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;

public class SearchPhotosAdapter extends RecyclerView.Adapter<SearchPhotosAdapter.PhotoViewHolder> {
    private final Context mContext;
    private final int mColorPrimaryWithAlpha;
    private final int mColorSecondaryWithAlpha;
    private final String mPicassoTag;
    private List<Photo> data;
    private PhotosActionListener mPhotosActionListener;

    public SearchPhotosAdapter(Context context, @NonNull List<Photo> photos, String picassoTag) {
        mContext = context;
        mPicassoTag = picassoTag;
        mColorPrimaryWithAlpha = Utils.adjustAlpha(CurrentTheme.getColorPrimary(mContext), 0.75F);
        mColorSecondaryWithAlpha = Utils.adjustAlpha(CurrentTheme.getColorSecondary(mContext), 0.60F);

        data = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.vk_photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int adapterPosition) {
        bindPhotoViewHolder(holder, data.get(adapterPosition));
    }

    public void setData(List<Photo> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void bindPhotoViewHolder(PhotoViewHolder holder, Photo photo) {
        holder.tvLike.setText(AppTextUtils.getCounterWithK(photo.getLikesCount()));
        holder.tvLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        holder.ivLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);

        holder.ivDownload.setVisibility(View.GONE);

        holder.tvComment.setText(AppTextUtils.getCounterWithK(photo.getCommentsCount()));
        holder.tvComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);
        holder.ivComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        holder.bottomRoot.setBackgroundColor(mColorPrimaryWithAlpha);
        holder.bottomRoot.setVisibility(photo.getLikesCount() + photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        holder.bottomTop.setVisibility(View.VISIBLE);
        holder.bottomTop.setBackgroundColor(mColorSecondaryWithAlpha);
        holder.tvDate.setText(AppTextUtils.getDateFromUnixTimeShorted(mContext, photo.getDate()));

        holder.setSelected(false);

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
                mPhotosActionListener.onPhotoClick(holder, photo);
            }
        };

        holder.photoImageView.setOnClickListener(clickListener);
        holder.index.setOnClickListener(clickListener);
        holder.darkView.setOnClickListener(clickListener);
    }

    public void setPhotosActionListener(PhotosActionListener photosActionListener) {
        mPhotosActionListener = photosActionListener;
    }

    public interface PhotosActionListener {
        void onPhotoClick(PhotoViewHolder holder, Photo photo);
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
        }

        public void setSelected(boolean selected) {
            index.setVisibility(selected ? View.VISIBLE : View.GONE);
            darkView.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }
}
