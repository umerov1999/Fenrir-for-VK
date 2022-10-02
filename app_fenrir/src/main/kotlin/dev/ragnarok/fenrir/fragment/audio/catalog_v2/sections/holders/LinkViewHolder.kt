package dev.ragnarok.fenrir.fragment.audio.catalog_v2.sections.holders

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.link.LinkHelper.openUrl
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.catalog_v2_audio.CatalogV2Link
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar

class LinkViewHolder(itemView: View) : IViewHolder(itemView) {
    val ivImage: ImageView = itemView.findViewById(R.id.item_link_pic)
    val tvTitle: TextView = itemView.findViewById(R.id.item_link_name)
    val tvDescription: TextView = itemView.findViewById(R.id.item_link_description)

    private val transformation: Transformation by lazy {
        CurrentTheme.createTransformationForAvatar()
    }

    override fun bind(position: Int, itemDataHolder: AbsModel) {
        val item = itemDataHolder as CatalogV2Link
        if (item.title.isNullOrEmpty()) tvTitle.visibility = View.INVISIBLE else {
            tvTitle.visibility = View.VISIBLE
            tvTitle.text = item.title
        }
        if (item.subtitle.isNullOrEmpty()) tvDescription.visibility =
            View.INVISIBLE else {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = item.subtitle
        }
        if (item.preview_photo != null) {
            displayAvatar(ivImage, transformation, item.preview_photo, Constants.PICASSO_TAG)
        } else {
            PicassoInstance.with().cancelRequest(ivImage)
            ivImage.setImageResource(R.drawable.ic_avatar_unknown)
        }
        itemView.setOnClickListener {
            if (itemView.context is Activity) {
                openUrl(
                    itemView.context as Activity,
                    Settings.get().accounts().current,
                    item.url,
                    false
                )
            }
        }
    }

    class Fabric : ViewHolderFabric {
        override fun create(view: View): IViewHolder {
            return LinkViewHolder(
                view
            )
        }

        override fun getLayout(): Int {
            return R.layout.item_catalog_v2_link
        }
    }
}
