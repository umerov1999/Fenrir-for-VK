package dev.ragnarok.fenrir.adapter;

import android.annotation.SuppressLint;
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
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class AudioPlaylistsAdapter extends RecyclerView.Adapter<AudioPlaylistsAdapter.Holder> {

    private final Context context;
    private final boolean isDark;
    private final boolean isSelect;
    private List<AudioPlaylist> data;
    private RecyclerView recyclerView;
    private ClickListener clickListener;

    public AudioPlaylistsAdapter(List<AudioPlaylist> data, Context context, boolean isSelect) {
        this.data = data;
        this.context = context;
        isDark = Settings.get().ui().isDarkModeEnabled(context);
        this.isSelect = isSelect;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_audio_playlist, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AudioPlaylist playlist = data.get(position);
        if (!Utils.isEmpty(playlist.getThumb_image()))
            ViewUtils.displayAvatar(holder.thumb, null, playlist.getThumb_image(), Constants.PICASSO_TAG);
        else
            holder.thumb.setImageResource(isDark ? R.drawable.generic_audio_nowplaying_dark : R.drawable.generic_audio_nowplaying_light);
        holder.count.setText(playlist.getCount() + " " + context.getString(R.string.audios_pattern_count));
        holder.name.setText(playlist.getTitle());
        if (Utils.isEmpty(playlist.getDescription()))
            holder.description.setVisibility(View.GONE);
        else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(playlist.getDescription());
        }
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
        if (Utils.isEmpty(playlist.getGenre()))
            holder.genre.setVisibility(View.GONE);
        else {
            holder.genre.setVisibility(View.VISIBLE);
            holder.genre.setText(playlist.getGenre());
        }
        holder.update.setText(AppTextUtils.getDateFromUnixTime(context, playlist.getUpdate_time()));
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

        void onOpenClick(int index, AudioPlaylist album);

        void onDelete(int index, AudioPlaylist album);

        void onShare(int index, AudioPlaylist album);

        void onEdit(int index, AudioPlaylist album);

        void onAddAudios(int index, AudioPlaylist album);

        void onAdd(int index, AudioPlaylist album, boolean clone);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        final ImageView thumb;
        final TextView name;
        final TextView description;
        final TextView count;
        final TextView year;
        final TextView artist;
        final TextView genre;
        final TextView update;
        final View playlist_container;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);
            thumb = itemView.findViewById(R.id.item_thumb);
            name = itemView.findViewById(R.id.item_name);
            count = itemView.findViewById(R.id.item_count);
            playlist_container = itemView.findViewById(R.id.playlist_container);
            description = itemView.findViewById(R.id.item_description);
            update = itemView.findViewById(R.id.item_time);
            year = itemView.findViewById(R.id.item_year);
            artist = itemView.findViewById(R.id.item_artist);
            genre = itemView.findViewById(R.id.item_genre);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = recyclerView.getChildAdapterPosition(v);
            AudioPlaylist playlist = data.get(position);

            if (!Utils.isValueAssigned(playlist.getId(), Settings.get().other().getServicePlaylist())) {
                if (Settings.get().accounts().getCurrent() == playlist.getOwnerId()) {
                    menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                        if (clickListener != null) {
                            clickListener.onDelete(position, playlist);
                        }
                        return true;
                    });
                    if (Utils.isEmpty(playlist.getOriginal_access_key()) || playlist.getOriginal_id() == 0 || playlist.getOriginal_owner_id() == 0) {
                        menu.add(0, v.getId(), 0, R.string.edit).setOnMenuItemClickListener(item -> {
                            if (clickListener != null) {
                                clickListener.onEdit(position, playlist);
                            }
                            return true;
                        });
                        menu.add(0, v.getId(), 0, R.string.action_add_audios).setOnMenuItemClickListener(item -> {
                            if (clickListener != null) {
                                clickListener.onAddAudios(position, playlist);
                            }
                            return true;
                        });
                    }
                } else {
                    menu.add(0, v.getId(), 0, R.string.save).setOnMenuItemClickListener(item -> {
                        if (clickListener != null) {
                            clickListener.onAdd(position, playlist, false);
                        }
                        return true;
                    });
                }
                if (!isSelect) {
                    menu.add(0, v.getId(), 0, R.string.share).setOnMenuItemClickListener(item -> {
                        if (clickListener != null) {
                            clickListener.onShare(position, playlist);
                        }
                        return true;
                    });
                }
            } else {
                menu.add(0, v.getId(), 0, R.string.save).setOnMenuItemClickListener(item -> {
                    if (clickListener != null) {
                        clickListener.onAdd(position, playlist, true);
                    }
                    return true;
                });
            }
            menu.add(0, v.getId(), 0, R.string.open).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onOpenClick(position, playlist);
                }
                return true;
            });
        }
    }
}
