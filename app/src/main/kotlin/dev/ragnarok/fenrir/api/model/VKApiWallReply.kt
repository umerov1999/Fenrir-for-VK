package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.orZero

class VKApiWallReply : VKApiAttachment {
    @SerializedName("id")
    var id = 0

    @SerializedName("from_id")
    var from_id = 0

    @SerializedName("post_id")
    var post_id = 0

    @SerializedName("owner_id")
    var owner_id = 0

    @SerializedName("text")
    var text: String? = null

    @SerializedName("attachments")
    var attachments: VKApiAttachments? = null
    val attachmentsCount: Int
        get() = attachments?.size().orZero()

    fun hasAttachments(): Boolean {
        return attachmentsCount > 0
    }

    override fun getType(): String {
        return VKApiAttachment.TYPE_WALL_REPLY
    }
}