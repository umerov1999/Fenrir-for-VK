package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.MemoryPolicy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.picasso.PicassoInstance;

public class LocalPhotosAdapter extends RecyclerView.Adapter<LocalPhotosAdapter.ViewHolder> {

    public static final String TAG = LocalPhotosAdapter.class.getSimpleName();

    private final Context context;
    private final List<LocalPhoto> data;
    private final Set<ViewHolder> holders;
    private ClickListener clickListener;

    public LocalPhotosAdapter(Context context, List<LocalPhoto> data) {
        this.context = context;
        this.data = data;
        holders = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.photo_item, parent, false));
        holders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalPhoto photo = data.get(position);
        holder.attachPhoto(photo);

        PicassoInstance.with()
                .load(PicassoInstance.buildUriForPicasso(Content_Local.PHOTO, photo.getImageId()))
                .tag(TAG)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        resolveSelectionVisibility(photo, holder);
        resolveIndexText(photo, holder);

        View.OnClickListener listener = v -> {
            if (clickListener != null) {
                clickListener.onPhotoClick(holder, photo);
            }
        };

        View.OnLongClickListener preview_listener = v -> {
            if (clickListener != null) {
                clickListener.onLongPhotoClick(holder, photo);
                return true;
            }
            return false;
        };

        holder.photoImageView.setOnClickListener(listener);
        holder.photoImageView.setOnLongClickListener(preview_listener);
        holder.selectedRoot.setOnClickListener(listener);
    }

    public void updateHoldersSelectionAndIndexes() {
        for (ViewHolder holder : holders) {
            LocalPhoto photo = (LocalPhoto) holder.itemView.getTag();

            if (photo == null) {
                // TODO: 13.12.2017 Photo can bee null !!!! WTF?
                continue;
            }

            resolveSelectionVisibility(photo, holder);
            resolveIndexText(photo, holder);
        }
    }

    private void resolveSelectionVisibility(LocalPhoto photo, ViewHolder holder) {
        holder.selectedRoot.setVisibility(photo.isSelected() ? View.VISIBLE : View.GONE);
    }

    private void resolveIndexText(LocalPhoto photo, ViewHolder holder) {
        holder.tvIndex.setText(photo.getIndex() == 0 ? "" : String.valueOf(photo.getIndex()));
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface ClickListener {
        void onPhotoClick(ViewHolder holder, LocalPhoto photo);

        void onLongPhotoClick(ViewHolder holder, LocalPhoto photo);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView photoImageView;
        final View selectedRoot;
        final TextView tvIndex;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.imageView);
            selectedRoot = itemView.findViewById(R.id.selected);
            tvIndex = itemView.findViewById(R.id.item_photo_index);
        }

        private void attachPhoto(LocalPhoto photo) {
            itemView.setTag(photo);
        }
    }
}
