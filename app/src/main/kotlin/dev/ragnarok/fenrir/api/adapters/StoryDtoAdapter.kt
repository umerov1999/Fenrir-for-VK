package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import dev.ragnarok.fenrir.api.model.VKApiStory
import dev.ragnarok.fenrir.api.model.VKApiVideo
import java.lang.reflect.Type

class StoryDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiStory> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiStory {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val story = VKApiStory()
        val root = json.asJsonObject
        story.id = optInt(root, "id")
        story.owner_id = optInt(root, "owner_id")
        story.date = optInt(root, "owner_id").toLong()
        story.expires_at = optInt(root, "expires_at").toLong()
        story.is_expired = optBoolean(root, "is_expired")
        story.is_ads = optBoolean(root, "is_ads")
        if (hasObject(root, "photo")) {
            story.photo = context.deserialize(root["photo"], VKApiPhoto::class.java)
        }
        if (hasObject(root, "video")) {
            story.video = context.deserialize(root["video"], VKApiVideo::class.java)
        }
        if (hasObject(root, "parent_story")) {
            story.parent_story = context.deserialize(root["parent_story"], VKApiStory::class.java)
        }
        if (hasObject(root, "link")) {
            story.target_url = optString(root.getAsJsonObject("link"), "url")
        }
        return story
    }

    companion object {
        private val TAG = StoryDtoAdapter::class.java.simpleName
    }
}