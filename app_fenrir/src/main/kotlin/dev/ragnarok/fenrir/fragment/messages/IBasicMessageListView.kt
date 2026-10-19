package dev.ragnarok.fenrir.fragment.messages

import dev.ragnarok.fenrir.fragment.base.IAttachmentsPlacesView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.model.LastReadId
import dev.ragnarok.fenrir.model.Message

interface IBasicMessageListView : IMvpView, IAttachmentsPlacesView,
    IToastView {
    fun notifyItemsAdded(position: Int, count: Int)
    fun notifyItemsRemoved(position: Int, count: Int)
    fun notifyItemsChanged(index: Int, count: Int)
    fun notifyDataChanged()
    fun configNowVoiceMessagePlaying(
        id: Int,
        progress: Float,
        duration: Long,
        paused: Boolean,
        amin: Boolean,
        speed: Boolean
    )

    fun bindVoiceHolderById(
        holderId: Int,
        play: Boolean,
        paused: Boolean,
        progress: Float,
        duration: Long,
        amin: Boolean,
        speed: Boolean
    )

    fun disableVoicePlaying()
    fun showActionMode(
        title: String,
        canEdit: Boolean,
        canPin: Boolean,
        canStar: Boolean,
        doStar: Boolean,
        canSpam: Boolean
    )

    fun finishActionMode()
    fun displayMessages(accountId: Long, messages: MutableList<Message>, lastReadId: LastReadId)

    fun showPopupOptions(
        position: Int,
        x: Int,
        y: Int,
        canEdit: Boolean,
        canPin: Boolean,
        canStar: Boolean,
        doStar: Boolean,
        canSpam: Boolean
    )

    fun refetchReactionCache(accountId: Long)
}