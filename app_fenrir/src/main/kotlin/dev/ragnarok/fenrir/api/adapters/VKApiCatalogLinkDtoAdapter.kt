package dev.ragnarok.fenrir.api.adapters

import dev.ragnarok.fenrir.api.model.VKApiCatalogLink
import dev.ragnarok.fenrir.util.serializeble.json.JsonElement

class VKApiCatalogLinkDtoAdapter : AbsAdapter<VKApiCatalogLink>("VKApiCatalogLink") {
    @Throws(Exception::class)
    override fun deserialize(
        json: JsonElement
    ): VKApiCatalogLink {
        if (!checkObject(json)) {
            throw Exception("$TAG error parse object")
        }
        val dto = VKApiCatalogLink()
        val root = json.asJsonObject
        dto.url = optString(root, "url")
        dto.title = optString(root, "title")
        dto.subtitle = optString(root, "subtitle")
        if (hasArray(root, "image")) {
            val arr = root.getAsJsonArray("image")
            var max_res = 0
            for (i in arr.orEmpty()) {
                if (!checkObject(i)) {
                    continue
                }
                val res = i.asJsonObject
                val curr_res = optInt(res, "height") * optInt(res, "width")
                if (curr_res > max_res) {
                    max_res = curr_res
                    dto.preview_photo = optString(res, "url")
                }
            }
        }
        return dto
    }

    companion object {
        private val TAG = VKApiCatalogLinkDtoAdapter::class.java.simpleName
    }
}