package dev.ragnarok.fenrir.adapter.fave;

import android.content.Context;
import android.view.ContextMenu;
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

public class FaveVideosAdapter extends RecyclerView.Adapter<FaveVideosAdapter.Holder> {

    private final Context context;
    private List<Video> data;
    private RecyclerView recyclerView;
    private VideoOnClickListener videoOnClickListener;

    public FaveVideosAdapter(@NonNull Context context, @NonNull List<Video> data) {
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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public interface VideoOnClickListener {
        void onVideoClick(int position, Video video);

        void onDelete(int index, Video video);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        final View card;
        final ImageView image;
        final TextView videoLenght;
        final ImageView videoService;
        final TextView title;
        final TextView viewsCount;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);
            card = itemView.findViewById(R.id.card_view);
            image = itemView.findViewById(R.id.video_image);
            videoLenght = itemView.findViewById(R.id.video_lenght);
            videoService = itemView.findViewById(R.id.video_service);
            title = itemView.findViewById(R.id.title);
            viewsCount = itemView.findViewById(R.id.view_count);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = recyclerView.getChildAdapterPosition(v);
            Video video = data.get(position);
            menu.setHeaderTitle(video.getTitle());

            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                if (videoOnClickListener != null) {
                    videoOnClickListener.onDelete(position, video);
                }
                return true;
            });
        }
    }
}
