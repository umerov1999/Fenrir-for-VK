package dev.ragnarok.fenrir.push

import android.content.Context
import android.graphics.Bitmap
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.Mode
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.model.Conversation
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.push.NotificationUtils.loadRoundedImageRx
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.empty
import io.reactivex.rxjava3.core.Single

object ChatEntryFetcher {
    fun getRx(context: Context, accountId: Int, peerId: Int): Single<DialogInfo> {
        val app = context.applicationContext
        when (Peer.getType(peerId)) {
            Peer.USER, Peer.GROUP -> {
                val ownerId = Peer.toOwnerId(peerId)
                return OwnerInfo.getRx(app, accountId, ownerId)
                    .map { info: OwnerInfo ->
                        val owner = info.owner
                        val response = DialogInfo()
                        response.title = owner.fullName
                        response.img = owner.get100photoOrSmaller()
                        response.icon = info.avatar
                        response
                    }
            }
            Peer.CHAT -> return messages
                .getConversation(accountId, peerId, Mode.ANY).singleOrError()
                .flatMap { chat: Conversation ->
                    loadRoundedImageRx(app, chat.get100orSmallerAvatar(), R.drawable.ic_group_chat)
                        .map { Optional.wrap(it) }
                        .onErrorReturnItem(empty())
                        .map { optional: Optional<Bitmap> ->
                            val response = DialogInfo()
                            response.title = chat.title
                            response.img = chat.get100orSmallerAvatar()
                            response.icon = optional.get()
                            response
                        }
                }
        }
        throw UnsupportedOperationException()
    }

    class DialogInfo {
        var title: String? = null
        var img: String? = null
        var icon: Bitmap? = null
    }
}