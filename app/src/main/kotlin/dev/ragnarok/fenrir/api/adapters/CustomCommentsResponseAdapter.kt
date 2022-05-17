package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.response.CustomCommentsResponse
import java.lang.reflect.Type

class CustomCommentsResponseAdapter : AbsAdapter(), JsonDeserializer<CustomCommentsResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CustomCommentsResponse {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val response = CustomCommentsResponse()
        val root = json.asJsonObject
        val main = root["main"]
        if (checkObject(main)) {
            response.main = context.deserialize(main, CustomCommentsResponse.Main::class.java)
        } // "main": false (if has execute errors)
        if (root.has("first_id")) {
            val firstIdJson = root["first_id"]
            response.firstId = if (firstIdJson.isJsonNull) null else firstIdJson.asInt
        }
        if (root.has("last_id")) {
            val lastIdJson = root["last_id"]
            response.lastId = if (lastIdJson.isJsonNull) null else lastIdJson.asInt
        }
        if (root.has("admin_level")) {
            val adminLevelJson = root["admin_level"]
            response.admin_level = if (adminLevelJson.isJsonNull) null else adminLevelJson.asInt
        }
        return response
    }

    companion object {
        private val TAG = CustomCommentsResponseAdapter::class.java.simpleName
    }
}