package dev.ragnarok.fenrir.adapter.feedback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.picasso.PicassoInstance;

public class FeedbackPhotosAdapter extends RecyclerView.Adapter<FeedbackPhotosAdapter.ViewHolder> {
    private List<Photo> data;
    private PhotoSelectionListener photoSelectionListener;

    public FeedbackPhotosAdapter(Context context, List<Photo> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback_photo, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackPhotosAdapter.ViewHolder viewHolder, int position) {
        Photo photo = data.get(position);

        PicassoInstance.with()
                .load(photo.getUrlForSize(PhotoSize.X, false))
                .tag(Constants.PICASSO_TAG)
                .placeholder(R.drawable.background_gray)
                .into(viewHolder.photoImageView);

        viewHolder.photoImageView.setOnClickListener(v -> {
            if (photoSelectionListener != null) {
                photoSelectionListener.onPhotoClicked(viewHolder.getBindingAdapterPosition(), photo);
            }
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

    public interface PhotoSelectionListener {
        void onPhotoClicked(int position, Photo photo);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView photoImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.imageView);
        }
    }
}
