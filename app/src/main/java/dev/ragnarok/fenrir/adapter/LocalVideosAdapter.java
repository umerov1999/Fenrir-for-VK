package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.MemoryPolicy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.LocalVideo;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.view.AspectRatioImageView;

public class LocalVideosAdapter extends RecyclerView.Adapter<LocalVideosAdapter.ViewHolder> {

    public static final String TAG = LocalVideosAdapter.class.getSimpleName();

    private final Context context;
    private final List<LocalVideo> data;
    private final Set<ViewHolder> holders;
    private ClickListener clickListener;

    public LocalVideosAdapter(Context context, List<LocalVideo> data) {
        this.context = context;
        this.data = data;
        holders = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_local_video, parent, false));
        holders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalVideo video = data.get(position);
        holder.attachPhoto(video);

        PicassoInstance.with()
                .load(PicassoInstance.buildUriForPicasso(Content_Local.VIDEO, video.getId()))
                .tag(TAG)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        resolveSelectionVisibility(video, holder);
        resolveIndexText(video, holder);

        View.OnClickListener listener = v -> {
            if (clickListener != null) {
                clickListener.onVideoClick(holder, video);
            }
        };

        View.OnLongClickListener preview_listener = v -> {
            if (clickListener != null) {
                clickListener.onVideoLongClick(holder, video);
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
            LocalVideo video = (LocalVideo) holder.itemView.getTag();

            if (video == null) {
                // TODO: 13.12.2017 Photo can bee null !!!! WTF?
                continue;
            }

            resolveSelectionVisibility(video, holder);
            resolveIndexText(video, holder);
        }
    }

    private void resolveSelectionVisibility(LocalVideo video, ViewHolder holder) {
        holder.selectedRoot.setVisibility(video.isSelected() ? View.VISIBLE : View.GONE);
    }

    private void resolveIndexText(LocalVideo video, ViewHolder holder) {
        holder.tvTitle.setText(video.getTitle());
        holder.tvDuration.setText(video.getDuration() == 0 ? "" : AppTextUtils.getDurationStringMS(video.getDuration()));
        holder.tvIndex.setText(video.getIndex() == 0 ? "" : String.valueOf(video.getIndex()));
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface ClickListener {
        void onVideoClick(ViewHolder holder, LocalVideo video);

        void onVideoLongClick(ViewHolder holder, LocalVideo video);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final AspectRatioImageView photoImageView;
        final View selectedRoot;
        final TextView tvIndex;
        final TextView tvTitle;
        final TextView tvDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.item_video_album_image);
            selectedRoot = itemView.findViewById(R.id.selected);
            tvIndex = itemView.findViewById(R.id.item_video_index);
            tvTitle = itemView.findViewById(R.id.item_video_album_title);
            tvDuration = itemView.findViewById(R.id.item_video_album_count);
        }

        private void attachPhoto(LocalVideo video) {
            itemView.setTag(video);
        }
    }
}
