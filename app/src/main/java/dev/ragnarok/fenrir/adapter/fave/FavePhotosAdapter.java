package dev.ragnarok.fenrir.adapter.fave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class FavePhotosAdapter extends RecyclerView.Adapter<FavePhotosAdapter.ViewHolder> {

    private final int colorPrimary;
    private List<Photo> data;
    private PhotoSelectionListener photoSelectionListener;
    private PhotoConversationListener photoConversationListener;
    private int currentPosition = -1;

    public FavePhotosAdapter(Context context, List<Photo> data) {
        this.data = data;
        colorPrimary = CurrentTheme.getColorPrimary(context);
    }

    public void updateCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fave_photo, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull FavePhotosAdapter.ViewHolder viewHolder, int position) {
        Photo photo = data.get(position);

        viewHolder.tvLike.setText(AppTextUtils.getCounterWithK(photo.getLikesCount()));

        viewHolder.tvLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        viewHolder.ivLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);

        viewHolder.tvComment.setText(AppTextUtils.getCounterWithK(photo.getCommentsCount()));

        viewHolder.tvComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);
        viewHolder.ivComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        viewHolder.vgBottom.setBackgroundColor(Utils.adjustAlpha(colorPrimary, 0.75F));
        viewHolder.vgBottom.setVisibility(photo.getLikesCount() + photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        if (FenrirNative.isNativeLoaded()) {
            if (currentPosition == position) {
                viewHolder.current.setVisibility(View.VISIBLE);
                viewHolder.current.fromRes(R.raw.donater_fire, Utils.dp(100), Utils.dp(100), new int[]{0xFF812E, colorPrimary}, true);
                viewHolder.current.playAnimation();
            } else {
                viewHolder.current.setVisibility(View.GONE);
                viewHolder.current.clearAnimationDrawable();
            }
        }

        PicassoInstance.with()
                .load(photo.getUrlForSize(PhotoSize.X, false))
                .tag(Constants.PICASSO_TAG)
                .placeholder(R.drawable.background_gray)
                .into(viewHolder.photoImageView);

        viewHolder.cardView.setOnClickListener(v -> {
            if (photoSelectionListener != null) {
                photoSelectionListener.onPhotoClicked(viewHolder.getBindingAdapterPosition(), photo);
            }
        });
        viewHolder.cardView.setOnLongClickListener(v -> {
            if (photoConversationListener != null) {
                photoConversationListener.onGoPhotoConversation(photo);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Photo> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setPhotoSelectionListener(PhotoSelectionListener photoSelectionListener) {
        this.photoSelectionListener = photoSelectionListener;
    }

    public void setPhotoConversationListener(PhotoConversationListener photoConversationListener) {
        this.photoConversationListener = photoConversationListener;
    }

    public interface PhotoSelectionListener {
        void onPhotoClicked(int position, Photo photo);
    }

    public interface PhotoConversationListener {
        void onGoPhotoConversation(@NonNull Photo photo);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View cardView;
        final ImageView photoImageView;
        final ViewGroup vgBottom;
        final TextView tvLike;
        final TextView tvComment;
        final ImageView ivLike;
        final ImageView ivComment;
        final RLottieImageView current;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            photoImageView = itemView.findViewById(R.id.imageView);
            vgBottom = itemView.findViewById(R.id.vk_photo_item_bottom);
            ivLike = itemView.findViewById(R.id.vk_photo_item_like);
            tvLike = itemView.findViewById(R.id.vk_photo_item_like_counter);
            ivComment = itemView.findViewById(R.id.vk_photo_item_comment);
            tvComment = itemView.findViewById(R.id.vk_photo_item_comment_counter);
            current = itemView.findViewById(R.id.current);
        }
    }
}
