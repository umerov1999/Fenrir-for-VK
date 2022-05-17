package dev.ragnarok.fenrir.api.model

class VKApiNotSupported(val attachmentType: String, val bodyJson: String?) : VKApiAttachment {
    override fun getType(): String {
        return VKApiAttachment.TYPE_NOT_SUPPORT
    }
}