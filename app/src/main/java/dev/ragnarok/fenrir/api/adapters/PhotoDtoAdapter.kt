package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.CommentsDto
import dev.ragnarok.fenrir.api.model.PhotoSizeDto
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import java.lang.reflect.Type

class PhotoDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiPhoto> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiPhoto {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val photo = VKApiPhoto()
        val root = json.asJsonObject
        photo.id = optInt(root, "id")
        photo.album_id = optInt(root, "album_id")
        photo.date = optLong(root, "date")
        photo.height = optInt(root, "height")
        photo.width = optInt(root, "width")
        photo.owner_id = optInt(root, "owner_id")
        photo.text = optString(root, "text")
        photo.access_key = optString(root, "access_key")
        if (hasObject(root, "likes")) {
            val likesRoot = root["likes"].asJsonObject
            photo.likes = optInt(likesRoot, "count")
            photo.user_likes = optBoolean(likesRoot, "user_likes")
        }
        if (hasObject(root, "comments")) {
            photo.comments = context.deserialize(root["comments"], CommentsDto::class.java)
        }
        if (hasObject(root, "tags")) {
            val tagsRoot = root["tags"].asJsonObject
            photo.tags = optInt(tagsRoot, "count")
        }
        photo.can_comment = optBoolean(root, "can_comment")
        photo.post_id = optInt(root, "post_id")
        if (hasArray(root, "sizes")) {
            val sizesArray = root.getAsJsonArray("sizes")
            photo.sizes = ArrayList(sizesArray.size())
            for (i in 0 until sizesArray.size()) {
                if (!checkObject(sizesArray[i])) {
                    continue
                }
                val photoSizeDto: PhotoSizeDto =
                    context.deserialize(sizesArray[i].asJsonObject, PhotoSizeDto::class.java)
                photo.sizes.add(photoSizeDto)
                when (photoSizeDto.type) {
                    PhotoSizeDto.Type.O, PhotoSizeDto.Type.P, PhotoSizeDto.Type.Q, PhotoSizeDto.Type.R -> continue
                    else -> {
                        if (photo.width > photoSizeDto.width && photo.height > photoSizeDto.height) {
                            continue
                        }
                        photo.width = photoSizeDto.width
                        photo.height = photoSizeDto.height
                    }
                }
            }
        }
        return photo
    }

    companion object {
        private val TAG = PhotoDtoAdapter::class.java.simpleName
    }
}