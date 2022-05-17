package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiPrivacy
import dev.ragnarok.fenrir.api.model.VKApiVideoAlbum
import java.lang.reflect.Type

class VideoAlbumDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiVideoAlbum> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiVideoAlbum {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val album = VKApiVideoAlbum()
        val root = json.asJsonObject
        album.id = optInt(root, "id")
        album.owner_id = optInt(root, "owner_id")
        album.title = optString(root, "title")
        album.count = optInt(root, "count")
        album.updated_time = optInt(root, "updated_time").toLong()
        if (hasObject(root, "privacy_view")) {
            album.privacy = context.deserialize(root["privacy_view"], VKApiPrivacy::class.java)
        }
        if (hasArray(root, "image")) {
            val images = root.getAsJsonArray("image")
            for (i in 0 until images.size()) {
                if (!checkObject(images[i])) {
                    continue
                }
                if (images[i].asJsonObject["width"].asInt >= 800) {
                    album.image = images[i].asJsonObject["url"].asString
                    break
                }
            }
            if (album.image == null) {
                if (checkObject(images[images.size() - 1])) {
                    album.image = images[images.size() - 1].asJsonObject["url"].asString
                }
            }
        } else if (root.has("photo_800")) {
            album.image = optString(root, "photo_800")
        } else if (root.has("photo_320")) {
            album.image = optString(root, "photo_320")
        }
        return album
    }

    companion object {
        private val TAG = VideoAlbumDtoAdapter::class.java.simpleName
    }
}