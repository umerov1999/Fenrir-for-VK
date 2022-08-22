package dev.ragnarok.fenrir.fragment.audio.catalog_v1.linksincatalog

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.base.RecyclerBindableAdapter
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar
import java.util.*

class CatalogLinksAdapter(data: MutableList<Link>) :
    RecyclerBindableAdapter<Link, CatalogLinksAdapter.LinkViewHolder>(data) {
    private val transformation: Transformation = CurrentTheme.createTransformationForAvatar()
    private var mActionListner: ActionListener? = null
    fun setActionListener(listener: ActionListener?) {
        mActionListner = listener
    }

    private fun getImageUrl(link: Link): String? {
        if (link.photo == null && link.previewPhoto != null) return link.previewPhoto
        else if (link.photo != null && link.photo?.sizes != null) {
            val sizes = link.photo?.sizes
            return sizes?.getUrlForSize(Settings.get().main().prefPreviewImageSize, true)
        }
        return null
    }

    override fun onBindItemViewHolder(viewHolder: LinkViewHolder, position: Int, type: Int) {
        val item = getItem(position)
        if (item.title.isNullOrEmpty()) viewHolder.tvTitle.visibility = View.INVISIBLE else {
            viewHolder.tvTitle.visibility = View.VISIBLE
            viewHolder.tvTitle.text = item.title
        }
        if (item.description.isNullOrEmpty()) viewHolder.tvDescription.visibility =
            View.INVISIBLE else {
            viewHolder.tvDescription.visibility = View.VISIBLE
            viewHolder.tvDescription.text = item.description
        }
        val imageUrl = getImageUrl(item)
        if (imageUrl != null) {
            displayAvatar(viewHolder.ivImage, transformation, imageUrl, Constants.PICASSO_TAG)
        } else {
            with().cancelRequest(viewHolder.ivImage)
            viewHolder.ivImage.setImageResource(R.drawable.ic_avatar_unknown)
        }
        viewHolder.itemView.setOnClickListener {
            mActionListner?.onLinkClick(viewHolder.bindingAdapterPosition, item)
        }
    }

    override fun viewHolder(view: View, type: Int): LinkViewHolder {
        return LinkViewHolder(view)
    }

    override fun layoutId(type: Int): Int {
        return R.layout.item_catalog_link_v1
    }

    interface ActionListener : EventListener {
        fun onLinkClick(index: Int, doc: Link)
    }

    class LinkViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val ivImage: ImageView = root.findViewById(R.id.item_link_pic)
        val tvTitle: TextView = root.findViewById(R.id.item_link_name)
        val tvDescription: TextView = root.findViewById(R.id.item_link_description)
    }

}