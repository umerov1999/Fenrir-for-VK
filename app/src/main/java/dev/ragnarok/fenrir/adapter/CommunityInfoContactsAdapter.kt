package dev.ragnarok.fenrir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.Manager
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar
import dev.ragnarok.fenrir.util.ViewUtils.getOnlineIcon
import dev.ragnarok.fenrir.view.OnlineView

class CommunityInfoContactsAdapter(private var users: List<Manager>) :
    RecyclerView.Adapter<CommunityInfoContactsAdapter.Holder>() {
    private val transformation: Transformation = CurrentTheme.createTransformationForAvatar()
    private var actionListener: ActionListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_community_manager, parent, false)
        )
    }

    fun setActionListener(actionListener: ActionListener?) {
        this.actionListener = actionListener
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val manager = users[position]
        val user = manager.user
        holder.name.text = user.fullName
        displayAvatar(holder.avatar, transformation, user.maxSquareAvatar, Constants.PICASSO_TAG)
        val onlineRes =
            getOnlineIcon(user.isOnline, user.isOnlineMobile, user.platform, user.onlineApp)
        if (onlineRes != null) {
            holder.onlineView.setIcon(onlineRes)
            holder.onlineView.visibility = View.VISIBLE
        } else {
            holder.onlineView.visibility = View.GONE
        }
        @StringRes val roleTextRes: Int
        if (manager.contactInfo != null && manager.contactInfo.descriprion != null) holder.role.text =
            manager.contactInfo.descriprion else {
            roleTextRes = R.string.role_unknown
            holder.role.setText(roleTextRes)
        }
        holder.itemView.setOnClickListener {
            actionListener?.onManagerClick(user)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun setData(data: List<Manager>) {
        users = data
        notifyDataSetChanged()
    }

    interface ActionListener {
        fun onManagerClick(manager: User)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
        val onlineView: OnlineView = itemView.findViewById(R.id.online)
        val name: TextView = itemView.findViewById(R.id.name)
        val role: TextView = itemView.findViewById(R.id.role)
    }
}