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

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.VideoServiceIcons;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.Holder> {

    private final Context context;
    private List<Video> data;
    private VideoOnClickListener videoOnClickListener;

    public VideosAdapter(@NonNull Context context, @NonNull List<Video> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_fave_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Video video = data.get(position);

        holder.viewsCount.setText(String.valueOf(video.getViews()));
        holder.title.setText(video.getTitle());
        holder.videoLenght.setText(AppTextUtils.getDurationString(video.getDuration()));

        String photoUrl = video.getImage();

        if (Utils.nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.image);
        } else {
            PicassoInstance.with().cancelRequest(holder.image);
        }

        Integer serviceIcon = VideoServiceIcons.getIconByType(video.getPlatform());
        if (Objects.nonNull(serviceIcon)) {
            holder.videoService.setVisibility(View.VISIBLE);
            holder.videoService.setImageResource(serviceIcon);
        } else {
            holder.videoService.setVisibility(View.GONE);
        }

        holder.card.setOnClickListener(v -> {
            if (videoOnClickListener != null) {
                videoOnClickListener.onVideoClick(position, video);
            }
        });
        holder.card.setOnLongClickListener(v -> {
            if (videoOnClickListener != null) {
                return videoOnClickListener.onVideoLongClick(position, video);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setVideoOnClickListener(VideoOnClickListener videoOnClickListener) {
        this.videoOnClickListener = videoOnClickListener;
    }

    public void setData(List<Video> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public interface VideoOnClickListener {
        void onVideoClick(int position, Video video);

        boolean onVideoLongClick(int position, Video video);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final View card;
        final ImageView image;
        final TextView videoLenght;
        final ImageView videoService;
        final TextView title;
        final TextView viewsCount;

        public Holder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_view);
            image = itemView.findViewById(R.id.video_image);
            videoLenght = itemView.findViewById(R.id.video_lenght);
            videoService = itemView.findViewById(R.id.video_service);
            title = itemView.findViewById(R.id.title);
            viewsCount = itemView.findViewById(R.id.view_count);
        }
    }
}
