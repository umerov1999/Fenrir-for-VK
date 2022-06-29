package dev.ragnarok.fenrir.api.adapters

import dev.ragnarok.fenrir.api.model.VKApiTopic
import dev.ragnarok.fenrir.api.model.response.NewsfeedCommentsResponse.*
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.util.serializeble.json.JsonElement
import dev.ragnarok.fenrir.util.serializeble.json.decodeFromJsonElement

class NewsfeedCommentDtoAdapter : AbsAdapter<Dto>("Dto") {
    @Throws(Exception::class)
    override fun deserialize(
        json: JsonElement
    ): Dto {
        if (!checkObject(json)) {
            throw UnsupportedOperationException()
        }
        val root = json.asJsonObject
        when (optString(root, "type", "post")) {
            "photo" -> {
                return PhotoDto(kJson.decodeFromJsonElement(root))
            }
            "post" -> {
                return PostDto(kJson.decodeFromJsonElement(root))
            }
            "video" -> {
                return VideoDto(kJson.decodeFromJsonElement(root))
            }
            "topic" -> {
                val topic = VKApiTopic()
                topic.id = optInt(root, "post_id")
                if (root.has("to_id")) topic.owner_id = optInt(root, "to_id") else topic.owner_id =
                    optInt(root, "source_id")
                topic.title = optString(root, "text")
                topic.comments =
                    root["comments"]?.let {
                        kJson.decodeFromJsonElement(it)
                    }
                return TopicDto(topic)
            }
        }
        throw UnsupportedOperationException()
    }
}