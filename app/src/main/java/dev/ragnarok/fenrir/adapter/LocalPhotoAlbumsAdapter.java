package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.MemoryPolicy;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.Settings;

public class LocalPhotoAlbumsAdapter extends RecyclerView.Adapter<LocalPhotoAlbumsAdapter.Holder> {

    public static final String PICASSO_TAG = "LocalPhotoAlbumsAdapter.TAG";
    private @Content_Local
    final int type;
    private final boolean isDark;
    private List<LocalImageAlbum> data;
    private ClickListener clickListener;

    public LocalPhotoAlbumsAdapter(@NonNull Context context, List<LocalImageAlbum> data, @Content_Local int type) {
        this.data = data;
        this.type = type;
        isDark = Settings.get().ui().isDarkModeEnabled(context);
    }

    public void setData(List<LocalImageAlbum> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.local_album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        LocalImageAlbum album = data.get(position);

        Uri uri = PicassoInstance.buildUriForPicasso(type, album.getCoverImageId());
        if (type == Content_Local.AUDIO) {
            if (album.getId() != 0) {
                holder.title.setText(album.getName());
                holder.subtitle.setText(holder.itemView.getContext().getString(R.string.local_audios_count, album.getPhotoCount()));
                PicassoInstance.with()
                        .load(uri)
                        .tag(PICASSO_TAG)
                        .placeholder(isDark ? R.drawable.generic_audio_nowplaying_dark : R.drawable.generic_audio_nowplaying_light)
                        .error(isDark ? R.drawable.generic_audio_nowplaying_dark : R.drawable.generic_audio_nowplaying_light)
                        .into(holder.image);
            } else {
                PicassoInstance.with().cancelRequest(holder.image);
                holder.image.setImageResource(isDark ? R.drawable.generic_audio_nowplaying_dark : R.drawable.generic_audio_nowplaying_light);
                holder.title.setText(R.string.all_audios);
                holder.subtitle.setText("");
            }
        } else {
            holder.title.setText(album.getName());
            holder.subtitle.setText(holder.itemView.getContext().getString(R.string.photos_count, album.getPhotoCount()));
            PicassoInstance.with()
                    .load(uri)
                    .tag(PICASSO_TAG)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(LocalImageAlbum album);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final ImageView image;
        final TextView title;
        final TextView subtitle;

        public Holder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_local_album_cover);
            title = itemView.findViewById(R.id.item_local_album_name);
            subtitle = itemView.findViewById(R.id.counter);
        }
    }
}
