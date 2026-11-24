package dev.ragnarok.fenrir.fragment.localserver.videoslocalserver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.util.Utils

class LocalServerVideosAdapter(private var data: List<Video>) :
    RecyclerView.Adapter<LocalServerVideosAdapter.Holder>() {
    private var videoOnClickListener: VideoOnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.item_local_server_video, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val video = data[position]
        holder.title.text = video.title
        holder.description.text = video.description
        holder.videoLength.text = Utils.BytesToSize(video.duration)
        val photoUrl = video.image
        holder.image.visibility = if (photoUrl.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        if (photoUrl.nonNullNoEmpty()) {
            with()
                .load(photoUrl)
                .tag(Constants.PICASSO_TAG)
                .into(holder.image)
        } else {
            with().cancelRequest(holder.image)
        }
        holder.card.setOnClickListener {
            videoOnClickListener?.onVideoClick(holder.bindingAdapterPosition, video)
        }
        holder.card.setOnLongClickListener {
            videoOnClickListener?.onOpenVideoMenu(holder.bindingAdapterPosition, video)
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setVideoOnClickListener(videoOnClickListener: VideoOnClickListener?) {
        this.videoOnClickListener = videoOnClickListener
    }

    fun setData(data: List<Video>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface VideoOnClickListener {
        fun onVideoClick(position: Int, video: Video)
        fun onOpenVideoMenu(position: Int, video: Video)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.card_view)
        val image: ImageView = itemView.findViewById(R.id.video_image)
        val videoLength: TextView = itemView.findViewById(R.id.video_length)
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
    }
}