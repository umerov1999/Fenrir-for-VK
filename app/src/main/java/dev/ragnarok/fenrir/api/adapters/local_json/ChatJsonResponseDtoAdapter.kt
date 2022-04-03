package dev.ragnarok.fenrir.api.adapters.local_json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.adapters.AbsAdapter
import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.local_json.ChatJsonResponse
import java.lang.reflect.Type

class ChatJsonResponseDtoAdapter : AbsAdapter(), JsonDeserializer<ChatJsonResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ChatJsonResponse {
        val story = ChatJsonResponse()
        if (!checkObject(json)) {
            return story
        }
        val root = json.asJsonObject
        story.type = optString(root, "type")
        story.page_avatar = optString(root, "page_avatar")
        story.page_id = optInt(root, "page_id")
        story.page_instagram = optString(root, "page_instagram")
        story.page_phone_number = optString(root, "page_phone_number")
        story.page_site = optString(root, "page_site")
        story.page_title = optString(root, "page_title")
        story.version = context.deserialize(root["version"], ChatJsonResponse.Version::class.java)
        story.messages = parseArray(
            root.getAsJsonArray(story.type),
            VKApiMessage::class.java,
            context,
            emptyList()
        )
        return story
    }
}