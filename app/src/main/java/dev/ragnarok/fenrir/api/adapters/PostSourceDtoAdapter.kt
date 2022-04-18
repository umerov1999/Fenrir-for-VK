package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiPostSource
import java.lang.reflect.Type

class PostSourceDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiPostSource> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiPostSource {
        val root = json.asJsonObject
        val dto = VKApiPostSource()
        dto.type = VKApiPostSource.Type.parse(optString(root, "type"))
        dto.platform = optString(root, "platform")
        dto.data = VKApiPostSource.Data.parse(optString(root, "data"))
        dto.url = optString(root, "url")
        return dto
    }
}