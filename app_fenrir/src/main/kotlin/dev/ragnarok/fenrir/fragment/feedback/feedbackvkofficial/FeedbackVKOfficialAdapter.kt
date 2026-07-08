package dev.ragnarok.fenrir.fragment.feedback.feedbackvkofficial

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.ifNonNull
import dev.ragnarok.fenrir.ifNonNullNoEmpty
import dev.ragnarok.fenrir.link.internal.FeedbackLinkSpanFactory
import dev.ragnarok.fenrir.model.FeedbackVKOfficial
import dev.ragnarok.fenrir.model.FeedbackVKOfficialList
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoSize
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.place.PlaceFactory.getSimpleGalleryPlace
import dev.ragnarok.fenrir.requireNonNull
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppTextUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar

class FeedbackVKOfficialAdapter(
    private var data: FeedbackVKOfficialList?,
    private val context: Context
) : RecyclerView.Adapter<FeedbackVKOfficialAdapter.Holder>() {
    private val transformation: Transformation = CurrentTheme.createTransformationForAvatar()
    private val mStartOfToday: Long = Utils.startOfTodayMillis()
    private var clickListener: ClickListener? = null
    fun checkPosition(position: Int): Boolean {
        if (data == null) {
            return false
        }
        return position >= 0 && (data?.items?.size ?: -1) > position
    }

    fun getByPosition(position: Int): FeedbackVKOfficial? {
        return data?.items?.get(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.item_feedback_official, parent, false)
        )
    }

    private fun loadIcon(holder: Holder, page: FeedbackVKOfficial, isSmall: Boolean) {
        if (!isSmall) {
            holder.avatar.setOnClickListener { }
        }
        val iconRes = getIconResByType(page.iconType)
        if (iconRes == null && page.iconURL == null) {
            if (isSmall) {
                holder.small.visibility = View.VISIBLE
                holder.small.setImageResource(
                    if (Settings.get()
                            .main().isRunes_show
                    ) R.drawable.client_round else R.drawable.client_round_vk
                )
                Utils.setColorFilter(
                    holder.small, CurrentTheme.getColorPrimary(
                        context
                    )
                )
            } else {
                holder.small.visibility = View.INVISIBLE
                holder.avatar.setImageResource(
                    if (Settings.get()
                            .main().isRunes_show
                    ) R.drawable.client_round else R.drawable.client_round_vk
                )
                Utils.setColorFilter(
                    holder.avatar, CurrentTheme.getColorPrimary(
                        context
                    )
                )
            }
            return
        }
        holder.avatar.clearColorFilter()
        holder.small.clearColorFilter()
        if (iconRes == null) {
            if (isSmall) {
                holder.small.visibility = View.VISIBLE
                displayAvatar(holder.small, transformation, page.iconURL, Constants.PICASSO_TAG)
            } else {
                holder.small.visibility = View.INVISIBLE
                displayAvatar(holder.avatar, transformation, page.iconURL, Constants.PICASSO_TAG)
            }
            return
        }
        if (isSmall) {
            holder.small.visibility = View.VISIBLE
            holder.small.setImageResource(iconRes)
        } else {
            holder.small.visibility = View.INVISIBLE
            holder.avatar.setImageResource(iconRes)
        }
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    private fun getDivided(messageDateJavaTime: Long, previousMessageDateJavaTime: Long?): Int {
        val stCurrent = getStatus(messageDateJavaTime)
        return if (previousMessageDateJavaTime == null) {
            stCurrent
        } else {
            val stPrevious = getStatus(previousMessageDateJavaTime)
            if (stCurrent == stPrevious) {
                DIV_DISABLE
            } else {
                stCurrent
            }
        }
    }

    private fun getStatus(time: Long): Int {
        if (time >= mStartOfToday) {
            return DIV_TODAY
        }
        if (time >= mStartOfToday - 86400000) {
            return DIV_YESTERDAY
        }
        return if (time >= mStartOfToday - 864000000) {
            DIV_THIS_WEEK
        } else DIV_OLD
    }

    private fun fromHtml(source: String): CharSequence {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val items = data?.items ?: return
        val page = items[position]
        val previous = if (position == 0) null else items[position - 1]
        val lastMessageJavaTime = page.time * 1000
        when (getDivided(
            lastMessageJavaTime,
            if (previous == null) null else previous.time * 1000
        )) {
            DIV_DISABLE -> holder.mHeaderTitle.visibility = View.GONE
            DIV_OLD -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.dialog_day_older)
            }

            DIV_TODAY -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.dialog_day_today)
            }

            DIV_YESTERDAY -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.dialog_day_yesterday)
            }

            DIV_THIS_WEEK -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.dialog_day_ten_days)
            }
        }
        holder.small.visibility = View.INVISIBLE
        page.header.ifNonNullNoEmpty({ lit ->
            holder.name.visibility = View.VISIBLE
            val replace = SpannableStringBuilder(fromHtml(lit))
            holder.name.setText(
                FeedbackLinkSpanFactory.parseLinks(context, replace),
                TextView.BufferType.SPANNABLE
            )
            if (page.headerOwnerId != null) {
                if (page.headerOwnerAvatarUrl.nonNullNoEmpty()) {
                    with()
                        .load(page.headerOwnerAvatarUrl)
                        .tag(Constants.PICASSO_TAG)
                        .placeholder(R.drawable.background_gray)
                        .transform(transformation)
                        .into(holder.avatar)
                    holder.avatar.setOnClickListener {
                        page.headerOwnerId?.let { vit ->
                            clickListener?.openOwnerWall(vit)
                        }
                    }
                    loadIcon(holder, page, true)
                } else {
                    with().cancelRequest(holder.avatar)
                    loadIcon(holder, page, false)
                }
            } else {
                with().cancelRequest(holder.avatar)
                loadIcon(holder, page, false)
            }
        }, {
            holder.name.visibility = View.GONE
            loadIcon(holder, page, false)
        })
        page.text.ifNonNullNoEmpty({
            holder.description.visibility = View.VISIBLE
            val replace = SpannableStringBuilder(fromHtml(it))
            holder.description.setText(
                FeedbackLinkSpanFactory.parseLinks(context, replace),
                TextView.BufferType.SPANNABLE
            )
        }, { holder.description.visibility = View.GONE })
        page.footer.ifNonNullNoEmpty({
            holder.footer.visibility = View.VISIBLE
            val replace = SpannableStringBuilder(fromHtml(it))
            holder.footer.setText(
                FeedbackLinkSpanFactory.parseLinks(context, replace),
                TextView.BufferType.SPANNABLE
            )
        }, { holder.footer.visibility = View.GONE })
        holder.time.text = AppTextUtils.getDateFromUnixTime(context, page.time)
        val img = page.getImage(256)
        if (img == null) {
            holder.additional.setOnClickListener { }
            holder.additional.visibility = View.GONE
            with().cancelRequest(holder.additional)
        } else {
            holder.additional.setOnClickListener {
                page.imagesAction.requireNonNull { sc ->
                    clickListener?.openAction(sc)
                }
            }
            holder.additional.visibility = View.VISIBLE
            with()
                .load(img.url)
                .tag(Constants.PICASSO_TAG)
                .placeholder(R.drawable.background_gray)
                .into(holder.additional)
        }
        when {
            page.attachments.isNullOrEmpty() -> {
                with().cancelRequest(holder.photoImage)
                holder.photoImage.visibility = View.GONE
                holder.attachments.visibility = View.GONE
                holder.attachments.adapter = null
            }

            page.attachments?.size == 1 -> {
                holder.photoImage.visibility = View.VISIBLE
                holder.attachments.visibility = View.GONE
                holder.attachments.adapter = null
                with()
                    .load(page.attachments?.get(0)?.getUrlForSize(PhotoSize.X, false))
                    .tag(Constants.PICASSO_TAG)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImage)
                holder.photoImage.setOnClickListener {
                    page.attachments?.get(0)?.let { r ->
                        getSimpleGalleryPlace(
                            Settings.get().accounts().current, ArrayList(
                                listOf(r)
                            ), 0, true
                        ).tryOpenWith(
                            context
                        )
                    }
                }
            }

            else -> {
                with().cancelRequest(holder.photoImage)
                holder.photoImage.visibility = View.GONE
                holder.attachments.visibility = View.VISIBLE
                holder.attachments.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                val adapter = FeedbackPhotosAdapter(page.attachments.orEmpty())
                adapter.setPhotoSelectionListener(object :
                    FeedbackPhotosAdapter.PhotoSelectionListener {
                    override fun onPhotoClicked(position: Int, photo: Photo) {
                        page.attachments.nonNullNoEmpty {
                            getSimpleGalleryPlace(
                                Settings.get().accounts().current,
                                ArrayList(it),
                                position,
                                true
                            ).tryOpenWith(
                                context
                            )
                        }
                    }
                })
                holder.attachments.adapter = adapter
            }
        }
        page.action.ifNonNull({
            if (it.getActionType() == FeedbackVKOfficial.ActionTypes.URL || it.getActionType() == FeedbackVKOfficial.ActionTypes.BROWSER_URL) {
                holder.actionButton.setText(R.string.more_info)
            } else {
                holder.actionButton.setText(R.string.open)
            }
            holder.actionButton.visibility = View.VISIBLE
        }, {
            holder.actionButton.visibility = View.GONE
        })
        holder.actionButton.setOnClickListener {
            page.action.requireNonNull {
                clickListener?.openAction(it)
            }
        }
    }

    private fun getIconResByType(iconType: String?): Int? {
        if (iconType.isNullOrEmpty()) {
            return null
        }
        return when (iconType) {
            "suggested_post_published" -> {
                R.drawable.ic_feedback_suggested_post_published
            }

            "transfer_money_cancelled" -> {
                R.drawable.ic_feedback_transfer_money_cancelled
            }

            "invite_game" -> {
                R.drawable.ic_feedback_invite_app
            }

            "cancel" -> {
                R.drawable.ic_feedback_cancel
            }

            "follow" -> {
                R.drawable.ic_feedback_follow
            }

            "repost" -> {
                R.drawable.ic_feedback_repost
            }

            "story_reply" -> {
                R.drawable.ic_feedback_story_reply
            }

            "photo_tag" -> {
                R.drawable.ic_feedback_photo_tag
            }

            "invite_group_accepted" -> {
                R.drawable.ic_feedback_friend_accepted
            }

            "ads" -> {
                R.drawable.ic_feedback_ads
            }

            "like" -> {
                R.drawable.ic_feedback_like
            }

            "live" -> {
                R.drawable.ic_feedback_live
            }

            "poll" -> {
                R.drawable.ic_feedback_poll
            }

            "wall" -> {
                R.drawable.ic_feedback_wall
            }

            "friend_found" -> {
                R.drawable.ic_feedback_add
            }

            "event" -> {
                R.drawable.ic_feedback_event
            }

            "reply" -> {
                R.drawable.ic_feedback_reply
            }

            "gift" -> {
                R.drawable.ic_feedback_gift
            }

            "friend_suggest" -> {
                R.drawable.ic_feedback_follow
            }

            "invite_group" -> {
                R.drawable.ic_feedback_invite_group
            }

            "friend_accepted" -> {
                R.drawable.ic_feedback_friend_accepted
            }

            "mention" -> {
                R.drawable.ic_feedback_mention
            }

            "comment" -> {
                R.drawable.ic_feedback_comment
            }

            "message" -> {
                R.drawable.ic_feedback_message
            }

            "private_post" -> {
                R.drawable.ic_feedback_private_post
            }

            "birthday" -> {
                R.drawable.ic_feedback_birthday
            }

            "invite_app" -> {
                R.drawable.ic_feedback_invite_app
            }

            "new_post" -> {
                R.drawable.ic_feedback_new_post
            }

            "interesting" -> {
                R.drawable.ic_feedback_interesting
            }

            "transfer_money" -> {
                R.drawable.ic_feedback_transfer_money
            }

            "transfer_votes" -> {
                R.drawable.ic_feedback_transfer_votes
            }

            else -> null
        }
    }

    override fun getItemCount(): Int {
        return data?.items?.size.orZero()
    }

    fun setData(data: FeedbackVKOfficialList) {
        this.data = data
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun openOwnerWall(ownerId: Long)
        fun openAction(action: FeedbackVKOfficial.Action)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.item_avatar)
        val name: TextView = itemView.findViewById(R.id.item_name)
        val description: TextView = itemView.findViewById(R.id.item_description)
        val footer: TextView = itemView.findViewById(R.id.item_footer)
        val time: TextView = itemView.findViewById(R.id.item_time)
        val small: ImageView = itemView.findViewById(R.id.item_icon)
        val mHeaderTitle: TextView = itemView.findViewById(R.id.header_title)
        val additional: ShapeableImageView = itemView.findViewById(R.id.additional_image)
        val photoImage: ShapeableImageView = itemView.findViewById(R.id.photo_image)
        val attachments: RecyclerView = itemView.findViewById(R.id.attachments)
        val actionButton: MaterialButton = itemView.findViewById(R.id.action_button)

        init {
            name.movementMethod = LinkMovementMethod.getInstance()
            description.movementMethod = LinkMovementMethod.getInstance()
            footer.movementMethod = LinkMovementMethod.getInstance()

            val fontSize = Settings.get().main().fontSize
            if (Settings.get().main().fontSizeOnlyForChatsAndMessages && fontSize != 0) {
                name.setTextSize(0, name.textSize + Utils.dp(0.4f) * fontSize)
                description.setTextSize(0, description.textSize + Utils.dp(0.4f) * fontSize)
                footer.setTextSize(0, footer.textSize + Utils.dp(0.4f) * fontSize)
                time.setTextSize(0, time.textSize + Utils.dp(0.4f) * fontSize)
                mHeaderTitle.setTextSize(0, mHeaderTitle.textSize + Utils.dp(0.4f) * fontSize)
            }
        }
    }

    companion object {
        private const val DIV_DISABLE = 0
        private const val DIV_TODAY = 1
        private const val DIV_YESTERDAY = 2
        private const val DIV_THIS_WEEK = 3
        private const val DIV_OLD = 4
    }

}
