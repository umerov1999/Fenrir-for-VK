package dev.ragnarok.fenrir.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MessageForwardSend {
    @SerialName("is_reply")
    var isReply = false
        private set

    @SerialName("peer_id")
    var peerId = 0L
        private set

    @SerialName("conversation_message_ids")
    var cmIds: List<Int>? = null
        private set

    fun setIsReply(isReply: Boolean): MessageForwardSend {
        this.isReply = isReply
        return this
    }

    fun setPeerId(peerId: Long): MessageForwardSend {
        this.peerId = peerId
        return this
    }

    fun setCmIds(cmIds: List<Int>?): MessageForwardSend {
        this.cmIds = cmIds
        return this
    }

}