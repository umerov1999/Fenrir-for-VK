package dev.ragnarok.fenrir.adapter.fave

import android.content.Context
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.SelectionUtils.addSelectionProfileSupport
import dev.ragnarok.fenrir.model.FavePage
import dev.ragnarok.fenrir.model.FavePageType
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar
import dev.ragnarok.fenrir.util.ViewUtils.getOnlineIcon
import dev.ragnarok.fenrir.view.AspectRatioImageView
import dev.ragnarok.fenrir.view.OnlineView

class FavePagesAdapter(private var data: List<FavePage>, private val context: Context) :
    RecyclerView.Adapter<FavePagesAdapter.Holder>() {
    private val transformation: Transformation = CurrentTheme.createTransformationForAvatar()
    private var recyclerView: RecyclerView? = null
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_fave_page, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val favePage = data[position]
        if (Settings.get().other().isMention_fave) {
            holder.itemView.setOnLongClickListener {
                if (favePage.id >= 0) {
                    clickListener?.onMention(favePage.owner)
                }
                true
            }
        }
        holder.description.text = favePage.description
        holder.name.text = favePage.owner.fullName
        holder.name.setTextColor(Utils.getVerifiedColor(context, favePage.owner.isVerified))
        holder.ivVerified.visibility = if (favePage.owner.isVerified) View.VISIBLE else View.GONE
        displayAvatar(
            holder.avatar,
            transformation,
            favePage.owner.maxSquareAvatar,
            Constants.PICASSO_TAG
        )
        if (favePage.type == FavePageType.USER) {
            holder.ivOnline.visibility = View.VISIBLE
            val user = favePage.user
            holder.blacklisted.visibility = if (user.blacklisted) View.VISIBLE else View.GONE
            val onlineIcon = getOnlineIcon(true, user.isOnlineMobile, user.platform, user.onlineApp)
            if (!user.isOnline) holder.ivOnline.setCircleColor(
                CurrentTheme.getColorFromAttrs(
                    R.attr.icon_color_inactive,
                    context,
                    "#000000"
                )
            ) else holder.ivOnline.setCircleColor(
                CurrentTheme.getColorFromAttrs(
                    R.attr.icon_color_active,
                    context,
                    "#000000"
                )
            )
            if (onlineIcon != null) {
                holder.ivOnline.setIcon(onlineIcon)
            }
        } else {
            holder.name.setTextColor(Utils.getVerifiedColor(context, false))
            holder.ivOnline.visibility = View.GONE
            holder.blacklisted.visibility = View.GONE
        }
        addSelectionProfileSupport(context, holder.avatar_root, favePage)
        holder.itemView.setOnClickListener {
            clickListener?.onPageClick(holder.bindingAdapterPosition, favePage.owner)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<FavePage>) {
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
        fun onPageClick(index: Int, owner: Owner)
        fun onDelete(index: Int, owner: Owner)
        fun onPushFirst(index: Int, owner: Owner)
        fun onMention(owner: Owner)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView),
        OnCreateContextMenuListener {
        val avatar_root: ViewGroup
        val avatar: AspectRatioImageView
        val blacklisted: ImageView
        val name: TextView
        val description: TextView
        val ivOnline: OnlineView
        val ivVerified: ImageView
        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
            val position = recyclerView?.getChildAdapterPosition(v) ?: 0
            val favePage = data[position]
            menu.setHeaderTitle(favePage.owner.fullName)
            menu.add(0, v.id, 0, R.string.delete).setOnMenuItemClickListener {
                clickListener?.onDelete(position, favePage.owner)
                true
            }
            menu.add(0, v.id, 0, R.string.push_first)
                .setOnMenuItemClickListener {
                    clickListener?.onPushFirst(position, favePage.owner)
                    true
                }
        }

        init {
            if (!Settings.get().other().isMention_fave) {
                itemView.setOnCreateContextMenuListener(this)
            }
            ivOnline = itemView.findViewById(R.id.header_navi_menu_online)
            avatar = itemView.findViewById(R.id.avatar)
            name = itemView.findViewById(R.id.name)
            description = itemView.findViewById(R.id.description)
            blacklisted = itemView.findViewById(R.id.item_blacklisted)
            ivVerified = itemView.findViewById(R.id.item_verified)
            avatar_root = itemView.findViewById(R.id.avatar_root)
        }
    }

}