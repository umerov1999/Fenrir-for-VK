package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiCatalogLink
import java.lang.reflect.Type

class VKApiCatalogLinkDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiCatalogLink> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiCatalogLink {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiCatalogLink()
        val root = json.asJsonObject
        dto.url = optString(root, "url")
        dto.title = optString(root, "title")
        dto.subtitle = optString(root, "subtitle")
        if (hasArray(root, "image")) {
            val arr = root.getAsJsonArray("image")
            var max_res = 0
            for (i in arr) {
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