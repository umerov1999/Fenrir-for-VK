package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse
import java.lang.reflect.Type

class ServicePlaylistResponseDtoAdapter : AbsAdapter(), JsonDeserializer<ServicePlaylistResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ServicePlaylistResponse {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val root = json.asJsonObject
        val dto = ServicePlaylistResponse()
        dto.playlists = ArrayList()
        if (checkArray(root["response"])) {
            val response = root.getAsJsonArray("response")
            for (i in response) {
                if (checkObject(i)) {
                    dto.playlists?.add(context.deserialize(i, VKApiAudioPlaylist::class.java))
                }
            }
        } else if (checkObject(root["response"])) {
            val response = root.getAsJsonObject("response")
            dto.playlists?.add(context.deserialize(response, VKApiAudioPlaylist::class.java))
        }
        return dto
    }

    companion object {
        private val TAG = ServicePlaylistResponseDtoAdapter::class.java.simpleName
    }
}