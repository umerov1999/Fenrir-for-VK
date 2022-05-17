package dev.ragnarok.fenrir.api.model

class VKApiCatalogLink : VKApiAttachment {
    var url: String? = null
    var title: String? = null
    var subtitle: String? = null
    var preview_photo: String? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_LINK
    }
}