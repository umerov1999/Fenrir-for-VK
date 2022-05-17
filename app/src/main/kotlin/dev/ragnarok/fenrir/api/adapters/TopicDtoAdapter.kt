package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.CommentsDto
import dev.ragnarok.fenrir.api.model.VKApiTopic
import java.lang.reflect.Type

class TopicDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiTopic> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiTopic {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiTopic()
        val root = json.asJsonObject
        dto.id = optInt(root, "id")
        dto.owner_id = optInt(root, "owner_id")
        dto.title = optString(root, "title")
        dto.created = optLong(root, "created")
        dto.created_by = optInt(root, "created_by")
        dto.updated = optInt(root, "updated").toLong()
        dto.updated_by = optInt(root, "updated_by")
        dto.is_closed = optBoolean(root, "is_closed")
        dto.is_fixed = optBoolean(root, "is_fixed")
        if (root.has("comments")) {
            val commentsJson = root["comments"]
            if (commentsJson.isJsonObject) {
                dto.comments = context.deserialize(commentsJson, CommentsDto::class.java)
            } else {
                dto.comments = CommentsDto()
                dto.comments?.count = commentsJson.asInt
            }
        }
        dto.first_comment = optString(root, "first_comment")
        dto.last_comment = optString(root, "last_comment")
        return dto
    }

    companion object {
        private val TAG = TopicDtoAdapter::class.java.simpleName
    }
}
