package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiNarratives
import java.lang.reflect.Type

class NarrativesDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiNarratives> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiNarratives {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiNarratives()
        val root = json.asJsonObject
        dto.id = optInt(root, "id")
        dto.owner_id = optInt(root, "owner_id")
        dto.title = optString(root, "title")
        if (hasArray(root, "story_ids")) {
            val temp = root.getAsJsonArray("story_ids")
            dto.story_ids = IntArray(temp.size()) { optInt(temp, it, 0) }
        }
        if (hasObject(root, "cover") && hasArray(root.getAsJsonObject("cover"), "cropped_sizes")) {
            val images = root.getAsJsonObject("cover").getAsJsonArray("cropped_sizes")
            for (i in 0 until images.size()) {
                if (!checkObject(images[i])) {
                    continue
                }
                if (images[i].asJsonObject["width"].asInt >= 400) {
                    dto.cover = images[i].asJsonObject["url"].asString
                    break
                }
            }
            if (dto.cover == null) {
                if (checkObject(images[images.size() - 1])) {
                    dto.cover = images[images.size() - 1].asJsonObject["url"].asString
                }
            }
        }
        return dto
    }

    companion object {
        private val TAG = NarrativesDtoAdapter::class.java.simpleName
    }
}
