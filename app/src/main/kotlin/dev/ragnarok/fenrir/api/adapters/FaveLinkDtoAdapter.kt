package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.FaveLinkDto
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import java.lang.reflect.Type

class FaveLinkDtoAdapter : AbsAdapter(), JsonDeserializer<FaveLinkDto> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FaveLinkDto {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val link = FaveLinkDto()
        var root = json.asJsonObject
        if (!hasObject(root, "link")) return link
        root = root["link"].asJsonObject
        link.id = optString(root, "id")
        link.description = optString(root, "description")
        if (hasObject(root, "photo")) {
            link.photo = context.deserialize(root["photo"], VKApiPhoto::class.java)
        }
        link.title = optString(root, "title")
        link.url = optString(root, "url")
        return link
    }

    companion object {
        private val TAG = FaveLinkDtoAdapter::class.java.simpleName
    }
}