package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiProfileInfoResponce
import java.lang.reflect.Type

class ProfileInfoResponceDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiProfileInfoResponce> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiProfileInfoResponce {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val info = VKApiProfileInfoResponce()
        val root = json.asJsonObject
        if (root.has("name_request")) {
            info.status = 2
        } else {
            info.status = if (optInt(root, "changed", 0) == 1) 1 else 0
        }
        return info
    }

    companion object {
        private val TAG = ProfileInfoResponceDtoAdapter::class.java.simpleName
    }
}