package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.feedback.UserArray
import java.lang.reflect.Type

class FeedbackUserArrayDtoAdapter : AbsAdapter(), JsonDeserializer<UserArray> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): UserArray {
        val root = json.asJsonObject
        val dto = UserArray()
        dto.count = optInt(root, "count")
        if (hasArray(root, "items")) {
            val array = root.getAsJsonArray("items")
            dto.ids = IntArray(array.size())
            for (i in 0 until array.size()) {
                if (!checkObject(array[i])) {
                    continue
                }
                dto.ids[i] = array[i].asJsonObject["from_id"].asInt
            }
        } else {
            dto.ids = IntArray(0)
        }
        return dto
    }
}