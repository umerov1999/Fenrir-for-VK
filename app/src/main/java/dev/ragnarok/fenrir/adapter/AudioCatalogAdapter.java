package dev.ragnarok.fenrir.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioCatalog;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.AspectRatioImageView;

public class AudioCatalogAdapter extends RecyclerView.Adapter<AudioCatalogAdapter.ViewHolder> implements AudioPlaylistsCatalogAdapter.ClickListener,
        AudioRecyclerAdapter.ClickListener, VideosAdapter.VideoOnClickListener, CatalogLinksAdapter.ActionListener {

    private final Context mContext;
    private final int account_id;
    private List<AudioCatalog> data;
    private ClickListener clickListener;

    public AudioCatalogAdapter(List<AudioCatalog> data, int account_id, Context context) {
        this.data = data;
        mContext = context;
        this.account_id = account_id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_catalog, parent, false));
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_catalog_artist, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).getArtist() == null)
            return 0;
        return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioCatalog category = data.get(position);

        if (category.getArtist() != null) {
            if (Utils.isEmpty(category.getArtist().getName()))
                holder.title.setVisibility(View.GONE);
            else {
                holder.title.setVisibility(View.VISIBLE);
                holder.title.setText(category.getArtist().getName());
            }
            if (holder.Image != null) {
                if (Utils.isEmpty(category.getArtist().getPhoto()))
                    PicassoInstance.with().cancelRequest(holder.Image);
                else
                    ViewUtils.displayAvatar(holder.Image, null, category.getArtist().getPhoto(), Constants.PICASSO_TAG);
            }
            return;
        }

        holder.catalog.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(position, category);
            }
        });

        if (Utils.isEmpty(category.getTitle()))
            holder.title.setVisibility(View.GONE);
        else {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(category.getTitle());
        }
        if (Utils.isEmpty(category.getSubtitle())) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(category.getSubtitle());
        }

        if (!Utils.isEmpty(category.getPlaylists())) {
            AudioPlaylistsCatalogAdapter adapter = new AudioPlaylistsCatalogAdapter(category.getPlaylists(), mContext);
            adapter.setClickListener(this);
            holder.list.setVisibility(View.VISIBLE);
            holder.list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            holder.list.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
            holder.list.setAdapter(adapter);
        } else if (!Utils.isEmpty(category.getAudios())) {
            Audio current = MusicPlaybackController.getCurrentAudio();
            int scroll_to = category.getAudios().indexOf(current);
            AudioRecyclerAdapter adapter = new AudioRecyclerAdapter(mContext, category.getAudios(), false, false, position, null);
            adapter.setClickListener(this);
            holder.list.setVisibility(View.VISIBLE);
            holder.list.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL));
            holder.list.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
            holder.list.setAdapter(adapter);
            if (scroll_to >= 0)
                holder.list.scrollToPosition(scroll_to);
        } else if (!Utils.isEmpty(category.getVideos())) {
            VideosAdapter adapter = new VideosAdapter(mContext, category.getVideos());
            adapter.setVideoOnClickListener(this);
            holder.list.setVisibility(View.VISIBLE);
            holder.list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            holder.list.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
            holder.list.setAdapter(adapter);
        } else if (!Utils.isEmpty(category.getLinks())) {
            CatalogLinksAdapter adapter = new CatalogLinksAdapter(category.getLinks());
            adapter.setActionListener(this);
            holder.list.setVisibility(View.VISIBLE);
            holder.list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            holder.list.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
            holder.list.setAdapter(adapter);
        } else
            holder.list.setVisibility(View.GONE);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<AudioCatalog> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public void onAlbumClick(int index, AudioPlaylist album) {
        if (Utils.isEmpty(album.getOriginal_access_key()) || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0)
            PlaceFactory.getAudiosInAlbumPlace(account_id, album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(mContext);
        else
            PlaceFactory.getAudiosInAlbumPlace(account_id, album.getOriginal_owner_id(), album.getOriginal_id(), album.getOriginal_access_key()).tryOpenWith(mContext);
    }

    @Override
    public void onDelete(int index, AudioPlaylist album) {

    }

    @Override
    public void onAdd(int index, AudioPlaylist album) {
        if (clickListener != null) {
            clickListener.onAddPlayList(index, album);
        }
    }

    @Override
    public void onClick(int position, int catalog, Audio audio) {
        MusicPlaybackService.startForPlayList(mContext, new ArrayList<>(data.get(catalog).getAudios()), position, false);
        if (!Settings.get().other().isShow_mini_player())
            PlaceFactory.getPlayerPlace(account_id).tryOpenWith(mContext);
    }

    @Override
    public void onEdit(int position, Audio audio) {

    }

    @Override
    public void onDelete(int position) {

    }

    @Override
    public void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix) {
        PlaceFactory.getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(mContext);
    }

    @Override
    public void onRequestWritePermissions() {
        if (clickListener != null) {
            clickListener.onRequestWritePermissions();
        }
    }

    @Override
    public void onVideoClick(int position, Video video) {
        PlaceFactory.getVideoPreviewPlace(account_id, video).tryOpenWith(mContext);
    }

    @Override
    public boolean onVideoLongClick(int position, Video video) {
        return false;
    }

    @Override
    public void onLinkClick(int index, @NonNull Link doc) {
        LinkHelper.openUrl((Activity) mContext, Settings.get().accounts().getCurrent(), doc.getUrl());
    }

    public interface ClickListener {
        void onClick(int index, AudioCatalog value);

        void onAddPlayList(int index, AudioPlaylist album);

        void onRequestWritePermissions();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final RecyclerView list;
        final AspectRatioImageView Image;
        final View catalog;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            subtitle = itemView.findViewById(R.id.item_subtitle);
            list = itemView.findViewById(R.id.list);
            Image = itemView.findViewById(R.id.item_image);
            catalog = itemView.findViewById(R.id.item_catalog_block);
        }
    }
}
