package dev.ragnarok.fenrir.fragment.audio.catalog_v1.playlistsincatalog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.transforms.ImageHelper.getEllipseBitmap
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar

class AudioPlaylistsCatalogAdapter(
    private var data: List<AudioPlaylist>,
    private val context: Context
) : RecyclerView.Adapter<AudioPlaylistsCatalogAdapter.Holder>() {
    private val isDark: Boolean = Settings.get().ui().isDarkModeEnabled(context)
    private var recyclerView: RecyclerView? = null
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.item_audio_playlist_catalog_v1, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val playlist = data[position]
        if (playlist.getThumb_image().nonNullNoEmpty()) displayAvatar(
            holder.thumb,
            PolyTransformation(),
            playlist.getThumb_image(),
            Constants.PICASSO_TAG
        ) else holder.thumb.setImageBitmap(
            getEllipseBitmap(
                BitmapFactory.decodeResource(
                    context.resources,
                    if (isDark) R.drawable.generic_audio_nowplaying_dark else R.drawable.generic_audio_nowplaying_light
                ), 0.1f
            )
        )
        holder.name.text = playlist.getTitle()
        if (playlist.getArtist_name().isNullOrEmpty()) holder.artist.visibility = View.GONE else {
            holder.artist.visibility = View.VISIBLE
            holder.artist.text = playlist.getArtist_name()
        }
        if (playlist.getYear() == 0) holder.year.visibility = View.GONE else {
            holder.year.visibility = View.VISIBLE
            holder.year.text = playlist.getYear().toString()
        }
        holder.playlist_container.setOnClickListener {
            clickListener?.onAlbumClick(holder.bindingAdapterPosition, playlist)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<AudioPlaylist>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onAlbumClick(index: Int, album: AudioPlaylist)
        fun onDelete(index: Int, album: AudioPlaylist)
        fun onAdd(index: Int, album: AudioPlaylist)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView),
        OnCreateContextMenuListener {
        val thumb: ImageView
        val name: TextView
        val year: TextView
        val artist: TextView
        val playlist_container: View
        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
            val position = recyclerView?.getChildAdapterPosition(v) ?: 0
            val playlist = data[position]
            if (Settings.get().accounts().current == playlist.getOwnerId()) {
                menu.add(0, v.id, 0, R.string.delete)
                    .setOnMenuItemClickListener {
                        clickListener?.onDelete(position, playlist)
                        true
                    }
            } else {
                menu.add(0, v.id, 0, R.string.save).setOnMenuItemClickListener {
                    clickListener?.onAdd(position, playlist)
                    true
                }
            }
        }

        init {
            itemView.setOnCreateContextMenuListener(this)
            thumb = itemView.findViewById(R.id.item_thumb)
            name = itemView.findViewById(R.id.item_name)
            playlist_container = itemView.findViewById(R.id.playlist_container)
            year = itemView.findViewById(R.id.item_year)
            artist = itemView.findViewById(R.id.item_artist)
        }
    }

}