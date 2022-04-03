package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.*
import java.lang.reflect.Type

class PostDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiPost> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiPost {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiPost()
        val root = json.asJsonObject
        dto.id = getFirstInt(root, 0, "post_id", "id")
        dto.post_type = VKApiPost.Type.parse(optString(root, "post_type"))
        dto.owner_id = getFirstInt(root, 0, "owner_id", "to_id", "source_id")
        dto.from_id = optInt(root, "from_id")
        if (dto.from_id == 0) {
            // "copy_history": [
            // {
            //     ... this post has been removed ...
            //     "id": 1032,
            //     "owner_id": 216143660,
            //     "from_id": 0,
            //     "date": 0,
            //     "post_type": "post",
            dto.from_id = dto.owner_id
        }
        dto.date = optLong(root, "date")
        dto.text = optString(root, "text")
        if (hasObject(root, "copyright")) {
            val cop = root.getAsJsonObject("copyright")
            if (dto.text.isNullOrEmpty()) {
                dto.text = ""
            }
            val name = optString(cop, "name")
            val link = optString(cop, "link")
            dto.text = """
                [$link|©$name]
                ${dto.text}
                """.trimIndent()
        }
        dto.reply_owner_id = optInt(root, "reply_owner_id", 0)
        if (dto.reply_owner_id == 0) {
            // for replies from newsfeed.search
            // но не помешало бы понять какого хе...а!!!
            dto.reply_owner_id = dto.owner_id
        }
        dto.reply_post_id = optInt(root, "reply_post_id", 0)
        if (dto.reply_post_id == 0) {
            // for replies from newsfeed.search
            // но не помешало бы понять какого хе...а (1)!!!
            dto.reply_post_id = optInt(root, "post_id")
        }
        dto.friends_only = optBoolean(root, "friends_only")
        if (hasObject(root, "comments")) {
            dto.comments = context.deserialize(root["comments"], CommentsDto::class.java)
        }
        if (hasObject(root, "likes")) {
            val likes = root.getAsJsonObject("likes")
            dto.likes_count = optInt(likes, "count")
            dto.user_likes = optBoolean(likes, "user_likes")
            dto.can_like = optBoolean(likes, "can_like")
            dto.can_publish = optBoolean(likes, "can_publish")
        }
        if (hasObject(root, "reposts")) {
            val reposts = root.getAsJsonObject("reposts")
            dto.reposts_count = optInt(reposts, "count")
            dto.user_reposted = optBoolean(reposts, "user_reposted")
        }
        if (hasObject(root, "views")) {
            val views = root.getAsJsonObject("views")
            dto.views = optInt(views, "count")
        }
        if (hasArray(root, "attachments")) {
            dto.attachments = context.deserialize(root["attachments"], VkApiAttachments::class.java)
        }
        if (hasObject(root, "geo")) {
            dto.geo = context.deserialize(root["geo"], VKApiPlace::class.java)
        }
        dto.can_edit = optBoolean(root, "can_edit")
        dto.signer_id = optInt(root, "signer_id")
        dto.created_by = optInt(root, "created_by")
        dto.can_pin = optInt(root, "can_pin") == 1
        dto.is_pinned = optBoolean(root, "is_pinned")
        if (hasArray(root, "copy_history")) {
            val copyHistoryArray = root.getAsJsonArray("copy_history")
            dto.copy_history = ArrayList(copyHistoryArray.size())
            for (i in 0 until copyHistoryArray.size()) {
                if (!checkObject(copyHistoryArray[i])) {
                    continue
                }
                val copy = copyHistoryArray[i].asJsonObject
                dto.copy_history.add(deserialize(copy, VKApiPost::class.java, context))
            }
        } else {
            //empty list
            dto.copy_history = ArrayList(0)
        }
        if (hasObject(root, "post_source")) {
            dto.post_source = context.deserialize(root["post_source"], VkApiPostSource::class.java)
        }
        return dto
    }

    companion object {
        private val TAG = PostDtoAdapter::class.java.simpleName
    }
}