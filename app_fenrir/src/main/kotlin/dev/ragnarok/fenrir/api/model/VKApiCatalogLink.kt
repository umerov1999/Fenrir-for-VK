package dev.ragnarok.fenrir.api.model

import dev.ragnarok.fenrir.api.adapters.VKApiCatalogLinkDtoAdapter
import kotlinx.serialization.Serializable

@Serializable(with = VKApiCatalogLinkDtoAdapter::class)
class VKApiCatalogLink : VKApiAttachment {
    var url: String? = null
    var title: String? = null
    var subtitle: String? = null
    var preview_photo: String? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_LINK
    }
}