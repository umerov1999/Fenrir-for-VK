package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

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
import dev.ragnarok.fenrir.model.VideoAlbum;
import dev.ragnarok.fenrir.picasso.PicassoInstance;

public class VideoAlbumsNewAdapter extends RecyclerView.Adapter<VideoAlbumsNewAdapter.ViewHolder> {

    public static final String PICASSO_TAG = "VideoAlbumsNewAdapter";

    private final Context context;
    private List<VideoAlbum> data;
    private Listener listener;

    public VideoAlbumsNewAdapter(Context context, List<VideoAlbum> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_video_album, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoAlbum item = data.get(position);

        holder.tvCount.setText(context.getString(R.string.videos_albums_videos_counter, item.getCount()));
        holder.tvTitle.setText(item.getTitle());
        String photoUrl = item.getImage();

        holder.ivPhoto.setVisibility(isEmpty(photoUrl) ? View.INVISIBLE : View.VISIBLE);

        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .tag(PICASSO_TAG)
                    .into(holder.ivPhoto);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<VideoAlbum> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onClick(VideoAlbum album);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final TextView tvCount;
        final TextView tvTitle;

        ViewHolder(View root) {
            super(root);
            ivPhoto = root.findViewById(R.id.item_video_album_image);
            tvCount = root.findViewById(R.id.item_video_album_count);
            tvTitle = root.findViewById(R.id.item_video_album_title);
        }
    }
}
