package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiChat
import dev.ragnarok.fenrir.api.model.response.ChatsInfoResponse
import java.lang.reflect.Type

class ChatsInfoAdapter : AbsAdapter(), JsonDeserializer<ChatsInfoResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ChatsInfoResponse {
        val chats: List<VKApiChat>? = when {
            checkObject(json) -> {
                listOf(context.deserialize(json, VKApiChat::class.java))
            }
            checkArray(json) -> {
                val array = json.asJsonArray
                parseArray(
                    array,
                    VKApiChat::class.java,
                    context,
                    emptyList()
                )
            }
            else -> {
                emptyList()
            }
        }
        val response = ChatsInfoResponse()
        response.chats = chats
        return response
    }
}