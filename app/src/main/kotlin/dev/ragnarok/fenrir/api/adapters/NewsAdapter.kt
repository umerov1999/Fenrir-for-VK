package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.*
import java.lang.reflect.Type

class NewsAdapter : AbsAdapter(), JsonDeserializer<VKApiNews> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiNews {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiNews()
        val root = json.asJsonObject
        dto.type = optString(root, "type")
        dto.source_id = optInt(root, "source_id")
        dto.date = optLong(root, "date")
        dto.post_id = optInt(root, "post_id")
        dto.post_type = optString(root, "post_type")
        dto.final_post = optBoolean(root, "final_post")
        dto.copy_owner_id = optInt(root, "copy_owner_id")
        dto.copy_post_id = optInt(root, "copy_post_id")
        dto.mark_as_ads = optInt(root, "mark_as_ads")
        if (hasArray(root, "copy_history")) {
            dto.copy_history = parseArray(
                root.getAsJsonArray("copy_history"),
                VKApiPost::class.java,
                context,
                emptyList()
            )
        } else {
            dto.copy_history = emptyList()
        }
        dto.copy_post_date = optLong(root, "copy_post_date")
        dto.text = optString(root, "text")
        dto.can_edit = optBoolean(root, "can_edit")
        dto.can_delete = optBoolean(root, "can_delete")
        if (hasObject(root, "comments")) {
            val commentsRoot = root.getAsJsonObject("comments")
            dto.comment_count = optInt(commentsRoot, "count")
            dto.comment_can_post = optBoolean(commentsRoot, "can_post")
        }
        if (hasObject(root, "likes")) {
            val likesRoot = root.getAsJsonObject("likes")
            dto.like_count = optInt(likesRoot, "count")
            dto.user_like = optBoolean(likesRoot, "user_likes")
            dto.can_like = optBoolean(likesRoot, "can_like")
            dto.can_publish = optBoolean(likesRoot, "can_publish")
        }
        if (hasObject(root, "reposts")) {
            val repostsRoot = root.getAsJsonObject("reposts")
            dto.reposts_count = optInt(repostsRoot, "count")
            dto.user_reposted = optBoolean(repostsRoot, "user_reposted")
        }
        if (hasObject(root, "views")) {
            val viewRoot = root.getAsJsonObject("views")
            dto.views = optInt(viewRoot, "count", 0)
        }
        if (hasArray(root, "attachments")) {
            dto.attachments = context.deserialize(root["attachments"], VKApiAttachments::class.java)
        }
        if (root.has("geo")) {
            dto.geo = context.deserialize(root["geo"], VKApiPlace::class.java)
        }
        if (root.has("photos")) {
            val photosArray = root.getAsJsonObject("photos").getAsJsonArray("items")
            if (dto.attachments == null) {
                dto.attachments = VKApiAttachments()
            }
            parseArray<VKApiPhoto>(photosArray, VKApiPhoto::class.java, context, null)?.let {
                dto.attachments?.append(it)
            }
        }
        if (root.has("photo_tags")) {
            val photosTagsArray = root.getAsJsonObject("photo_tags").getAsJsonArray("items")
            if (dto.attachments == null) {
                dto.attachments = VKApiAttachments()
            }
            parseArray<VKApiPhoto>(
                photosTagsArray,
                VKApiPhoto::class.java,
                context,
                null
            )?.let {
                dto.attachments?.append(it)
            }
        }
        if (root.has("audio")) {
            val photosTagsArray = root.getAsJsonObject("audio").getAsJsonArray("items")
            if (dto.attachments == null) {
                dto.attachments = VKApiAttachments()
            }
            parseArray<VKApiPhoto>(
                photosTagsArray,
                VKApiAudio::class.java,
                context,
                null
            )?.let {
                dto.attachments?.append(it)
            }
        }
        if (root.has("video")) {
            val photosTagsArray = root.getAsJsonObject("video").getAsJsonArray("items")
            if (dto.attachments == null) {
                dto.attachments = VKApiAttachments()
            }
            parseArray<VKApiPhoto>(
                photosTagsArray,
                VKApiVideo::class.java,
                context,
                null
            )?.let {
                dto.attachments?.append(it)
            }
        }
        if (root.has("friends")) {
            val friendsArray = root.getAsJsonObject("friends").getAsJsonArray("items")
            dto.friends = ArrayList(friendsArray.size())
            for (i in 0 until friendsArray.size()) {
                val friendObj = friendsArray[i].asJsonObject
                dto.friends?.add(friendObj["user_id"].asInt)
            }
        }
        return dto
    }

    companion object {
        private val TAG = NewsAdapter::class.java.simpleName
    }
}