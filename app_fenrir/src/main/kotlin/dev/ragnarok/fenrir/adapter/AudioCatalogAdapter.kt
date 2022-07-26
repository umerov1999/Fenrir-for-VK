package dev.ragnarok.fenrir.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.ifNonNullNoEmpty
import dev.ragnarok.fenrir.link.LinkHelper
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.module.StringHash
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosInAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getVideoPreviewPlace
import dev.ragnarok.fenrir.requireNonNull
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar
import dev.ragnarok.fenrir.view.AspectRatioImageView
import dev.ragnarok.fenrir.view.VP2NestedRecyclerView

class AudioCatalogAdapter(
    private var data: List<AudioCatalog>,
    private val account_id: Int,
    private val mContext: Context
) : RecyclerView.Adapter<AudioCatalogAdapter.ViewHolder>(),
    AudioPlaylistsCatalogAdapter.ClickListener, AudioRecyclerAdapter.ClickListener,
    VideosAdapter.VideoOnClickListener, CatalogLinksAdapter.ActionListener {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_audio_catalog, parent, false)
        ) else ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_audio_catalog_artist, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].getArtist() == null) 0 else 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = data[position]
        category.getArtist().requireNonNull {
            if (it.getName().isNullOrEmpty()) holder.title.visibility = View.GONE else {
                holder.title.visibility = View.VISIBLE
                holder.title.text = it.getName()
            }
            if (holder.Image != null) {
                if (it.getPhoto()
                        .isNullOrEmpty()
                ) with().cancelRequest(holder.Image) else displayAvatar(
                    holder.Image,
                    null,
                    it.getPhoto(),
                    Constants.PICASSO_TAG
                )
            }
            return
        }
        holder.catalog?.setOnClickListener {
            clickListener?.onClick(position, category)
        }
        if (category.getTitle().isNullOrEmpty()) holder.title.visibility = View.GONE else {
            holder.title.visibility = View.VISIBLE
            holder.title.text = category.getTitle()
        }
        if (category.getSubtitle().isNullOrEmpty()) {
            holder.subtitle?.visibility = View.GONE
        } else {
            holder.subtitle?.visibility = View.VISIBLE
            holder.subtitle?.text = category.getSubtitle()
        }
        category.getPlaylists().ifNonNullNoEmpty({
            val adapter = AudioPlaylistsCatalogAdapter(it, mContext)
            adapter.setClickListener(this)
            holder.list?.visibility = View.VISIBLE
            holder.list?.layoutManager =
                LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            holder.list?.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
            holder.list?.adapter = adapter
            holder.list?.updateUid(category.getId()?.let { it1 -> StringHash.calculateCRC32(it1) })
        }, {
            category.getAudios().ifNonNullNoEmpty({
                val current = currentAudio
                val scroll_to = it.indexOf(current)
                val adapter =
                    AudioRecyclerAdapter(
                        mContext, it,
                        not_show_my = false,
                        iSSelectMode = false,
                        iCatalogBlock = position,
                        playlist_id = null
                    )
                adapter.setClickListener(this)
                holder.list?.visibility = View.VISIBLE
                holder.list?.layoutManager =
                    StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)
                holder.list?.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
                holder.list?.adapter = adapter
                holder.list?.updateUid(
                    category.getId()?.let { it1 -> StringHash.calculateCRC32(it1) })
                if (scroll_to >= 0) holder.list?.scrollToPosition(scroll_to)
            }, {

                category.getVideos().ifNonNullNoEmpty({
                    val adapter = VideosAdapter(mContext, it)
                    adapter.setVideoOnClickListener(this)
                    holder.list?.visibility = View.VISIBLE
                    holder.list?.layoutManager =
                        LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
                    holder.list?.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
                    holder.list?.adapter = adapter
                    holder.list?.updateUid(
                        category.getId()?.let { it1 -> StringHash.calculateCRC32(it1) })
                }, {

                    category.getLinks().ifNonNullNoEmpty({
                        val adapter = CatalogLinksAdapter(it)
                        adapter.setActionListener(this)
                        holder.list?.visibility = View.VISIBLE
                        holder.list?.layoutManager =
                            LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
                        holder.list?.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
                        holder.list?.adapter = adapter
                        holder.list?.updateUid(category.getId()?.let { it1 ->
                            StringHash.calculateCRC32(
                                it1
                            )
                        })
                    }, { holder.list?.visibility = View.GONE })
                })
            })
        })
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<AudioCatalog>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onAlbumClick(index: Int, album: AudioPlaylist) {
        if (album.getOriginal_access_key()
                .isNullOrEmpty() || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0
        ) getAudiosInAlbumPlace(
            account_id, album.getOwnerId(), album.getId(), album.getAccess_key()
        ).tryOpenWith(mContext) else getAudiosInAlbumPlace(
            account_id,
            album.getOriginal_owner_id(),
            album.getOriginal_id(),
            album.getOriginal_access_key()
        ).tryOpenWith(mContext)
    }

    override fun onDelete(index: Int, album: AudioPlaylist) {}
    override fun onAdd(index: Int, album: AudioPlaylist) {
        clickListener?.onAddPlayList(index, album)
    }

    override fun onClick(position: Int, catalog: Int, audio: Audio) {
        data[catalog].getAudios()?.let {
            ArrayList(
                it
            )
        }?.let {
            startForPlayList(
                mContext, it, position, false
            )
        }
        if (!Settings.get().other().isShow_mini_player) getPlayerPlace(
            account_id
        ).tryOpenWith(mContext)
    }

    override fun onEdit(position: Int, audio: Audio) {}
    override fun onDelete(position: Int) {}
    override fun onUrlPhotoOpen(url: String, prefix: String, photo_prefix: String) {
        getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(mContext)
    }

    override fun onRequestWritePermissions() {
        clickListener?.onRequestWritePermissions()
    }

    override fun onVideoClick(position: Int, video: Video) {
        getVideoPreviewPlace(account_id, video).tryOpenWith(mContext)
    }

    override fun onVideoLongClick(position: Int, video: Video): Boolean {
        return false
    }

    override fun onLinkClick(index: Int, doc: Link) {
        LinkHelper.openUrl(mContext as Activity, Settings.get().accounts().current, doc.url, false)
    }

    interface ClickListener {
        fun onClick(index: Int, value: AudioCatalog)
        fun onAddPlayList(index: Int, album: AudioPlaylist)
        fun onRequestWritePermissions()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.item_title)
        val subtitle: TextView? = itemView.findViewById(R.id.item_subtitle)
        val list: VP2NestedRecyclerView? = itemView.findViewById(R.id.list)
        val Image: AspectRatioImageView? = itemView.findViewById(R.id.item_image)
        val catalog: View? = itemView.findViewById(R.id.item_catalog_block)
    }
}