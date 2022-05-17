package dev.ragnarok.fenrir.api.model

class LinkAttachmentToken(val url: String?) : IAttachmentToken {
    override fun format(): String {
        return url ?: "null"
    }
}