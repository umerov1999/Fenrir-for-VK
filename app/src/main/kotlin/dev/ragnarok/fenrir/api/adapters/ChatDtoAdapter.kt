package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.ChatUserDto
import dev.ragnarok.fenrir.api.model.VKApiChat
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiUser
import java.lang.reflect.Type

class ChatDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiChat> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiChat {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiChat()
        val root = json.asJsonObject
        dto.id = optInt(root, "id")
        dto.type = optString(root, "type")
        dto.title = optString(root, "title")
        dto.photo_50 = optString(root, "photo_50")
        dto.photo_100 = optString(root, "photo_100")
        dto.photo_200 = optString(root, "photo_200")
        dto.admin_id = optInt(root, "admin_id")
        if (hasArray(root, "users")) {
            val users = root.getAsJsonArray("users")
            dto.users = ArrayList(users.size())
            for (i in 0 until users.size()) {
                val userElement = users[i]
                if (userElement.isJsonPrimitive) {
                    val user = VKApiUser()
                    user.id = userElement.asInt
                    val chatUserDto = ChatUserDto()
                    chatUserDto.user = user
                    dto.users?.add(chatUserDto)
                } else {
                    if (!checkObject(userElement)) {
                        continue
                    }
                    val jsonObject = userElement.asJsonObject
                    val type = optString(jsonObject, "type")
                    val chatUserDto = ChatUserDto()
                    chatUserDto.type = type
                    chatUserDto.invited_by = optInt(jsonObject, "invited_by", 0)
                    if ("profile" == type) {
                        chatUserDto.user = context.deserialize(userElement, VKApiUser::class.java)
                    } else if ("group" == type) {
                        chatUserDto.user =
                            context.deserialize(userElement, VKApiCommunity::class.java)
                    } else {
                        //not supported
                        continue
                    }
                    dto.users?.add(chatUserDto)
                }
            }
        } else {
            dto.users = ArrayList(0)
        }
        return dto
    }

    companion object {
        private val TAG = ChatDtoAdapter::class.java.simpleName
    }
}