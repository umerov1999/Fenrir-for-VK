package dev.ragnarok.fenrir.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
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
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.picasso.transforms.ImageHelper;
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class AudioPlaylistsCatalogAdapter extends RecyclerView.Adapter<AudioPlaylistsCatalogAdapter.Holder> {

    private final Context context;
    private final boolean isDark;
    private List<AudioPlaylist> data;
    private RecyclerView recyclerView;
    private ClickListener clickListener;

    public AudioPlaylistsCatalogAdapter(List<AudioPlaylist> data, Context context) {
        this.data = data;
        this.context = context;
        isDark = Settings.get().ui().isDarkModeEnabled(context);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_audio_playlist_catalog, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AudioPlaylist playlist = data.get(position);
        if (!Utils.isEmpty(playlist.getThumb_image()))
            ViewUtils.displayAvatar(holder.thumb, new PolyTransformation(), playlist.getThumb_image(), Constants.PICASSO_TAG);
        else
            holder.thumb.setImageBitmap(ImageHelper.getEllipseBitmap(BitmapFactory.decodeResource(context.getResources(), isDark ? R.drawable.generic_audio_nowplaying_dark : R.drawable.generic_audio_nowplaying_light), 0.1f));
        holder.name.setText(playlist.getTitle());
        if (Utils.isEmpty(playlist.getArtist_name()))
            holder.artist.setVisibility(View.GONE);
        else {
            holder.artist.setVisibility(View.VISIBLE);
            holder.artist.setText(playlist.getArtist_name());
        }
        if (playlist.getYear() == 0)
            holder.year.setVisibility(View.GONE);
        else {
            holder.year.setVisibility(View.VISIBLE);
            holder.year.setText(String.valueOf(playlist.getYear()));
        }
        holder.playlist_container.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAlbumClick(holder.getBindingAdapterPosition(), playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<AudioPlaylist> data) {
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

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onAlbumClick(int index, AudioPlaylist album);

        void onDelete(int index, AudioPlaylist album);

        void onAdd(int index, AudioPlaylist album);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        final ImageView thumb;
        final TextView name;
        final TextView year;
        final TextView artist;
        final View playlist_container;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);
            thumb = itemView.findViewById(R.id.item_thumb);
            name = itemView.findViewById(R.id.item_name);
            playlist_container = itemView.findViewById(R.id.playlist_container);
            year = itemView.findViewById(R.id.item_year);
            artist = itemView.findViewById(R.id.item_artist);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = recyclerView.getChildAdapterPosition(v);
            AudioPlaylist playlist = data.get(position);

            if (Settings.get().accounts().getCurrent() == playlist.getOwnerId()) {
                menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                    if (clickListener != null) {
                        clickListener.onDelete(position, playlist);
                    }
                    return true;
                });
            } else {
                menu.add(0, v.getId(), 0, R.string.save).setOnMenuItemClickListener(item -> {
                    if (clickListener != null) {
                        clickListener.onAdd(position, playlist);
                    }
                    return true;
                });
            }
        }
    }
}
