package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent
import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates
import dev.ragnarok.fenrir.util.Logger
import java.lang.reflect.Type

class LongpollUpdatesAdapter : AbsAdapter(), JsonDeserializer<VkApiLongpollUpdates> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VkApiLongpollUpdates {
        val updates = VkApiLongpollUpdates()
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val root = json.asJsonObject
        updates.failed = optInt(root, "failed")
        updates.ts = optLong(root, "ts")
        val array = root["updates"]
        if (checkArray(array)) {
            for (i in 0 until array.asJsonArray.size()) {
                val updateArray = array.asJsonArray[i].asJsonArray
                val event: AbsLongpollEvent? =
                    context.deserialize(updateArray, AbsLongpollEvent::class.java)
                if (event != null) {
                    updates.putUpdate(event)
                } else {
                    Logger.d(TAG, "Unhandled Longpoll event: array: $updateArray")
                }
            }
        }
        return updates
    }

    companion object {
        private val TAG = LongpollUpdatesAdapter::class.java.simpleName
    }
}