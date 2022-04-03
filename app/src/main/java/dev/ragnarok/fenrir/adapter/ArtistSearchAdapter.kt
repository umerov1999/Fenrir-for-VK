package dev.ragnarok.fenrir.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VkApiArtist

class ArtistSearchAdapter(private var data: List<VkApiArtist>, private val context: Context) :
    RecyclerView.Adapter<ArtistSearchAdapter.Holder>() {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(context).inflate(R.layout.item_artist_search, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = data[position]
        if (item.name.isNullOrEmpty()) holder.tvTitle.visibility = View.GONE else {
            holder.tvTitle.visibility = View.VISIBLE
            holder.tvTitle.text = item.name
        }
        if (item.domain.isNullOrEmpty()) holder.tvDomain.visibility = View.GONE else {
            holder.tvDomain.visibility = View.VISIBLE
            holder.tvDomain.text = "@" + item.domain
        }
        if (item.id.isNullOrEmpty()) holder.tvId.visibility = View.GONE else {
            holder.tvId.visibility = View.VISIBLE
            holder.tvId.text = "id" + item.id
        }
        holder.itemView.setOnClickListener {
            clickListener?.onArtistClick(item.id)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<VkApiArtist>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onArtistClick(id: String)
    }

    class Holder(root: View) : RecyclerView.ViewHolder(root) {
        val tvTitle: TextView = root.findViewById(R.id.item_artist_search_title)
        val tvDomain: TextView = root.findViewById(R.id.item_artist_search_domain)
        val tvId: TextView = root.findViewById(R.id.item_artist_search_id)
    }
}