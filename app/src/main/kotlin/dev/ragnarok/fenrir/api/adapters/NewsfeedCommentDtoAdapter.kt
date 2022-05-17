package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.response.NewsfeedCommentsResponse.*
import java.lang.reflect.Type

class NewsfeedCommentDtoAdapter : AbsAdapter(), JsonDeserializer<Dto?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Dto? {
        var dto: Dto? = null
        if (!checkObject(json)) {
            return null
        }
        val root = json.asJsonObject
        when (optString(root, "type", "post")) {
            "photo" -> {
                dto = PhotoDto(context.deserialize(root, VKApiPhoto::class.java))
            }
            "post" -> {
                dto = PostDto(context.deserialize(root, VKApiPost::class.java))
            }
            "video" -> {
                dto = VideoDto(context.deserialize(root, VKApiVideo::class.java))
            }
            "topic" -> {
                val topic = VKApiTopic()
                topic.id = optInt(root, "post_id")
                if (root.has("to_id")) topic.owner_id = optInt(root, "to_id") else topic.owner_id =
                    optInt(root, "source_id")
                topic.title = optString(root, "text")
                topic.comments = context.deserialize(root["comments"], CommentsDto::class.java)
                dto = TopicDto(topic)
            }
        }
        return dto
    }
}