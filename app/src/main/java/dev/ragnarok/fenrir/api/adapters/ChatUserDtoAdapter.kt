package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.ChatUserDto
import dev.ragnarok.fenrir.api.model.VKApiUser
import java.lang.reflect.Type

class ChatUserDtoAdapter : AbsAdapter(), JsonDeserializer<ChatUserDto> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ChatUserDto {
        val user: VKApiUser = context.deserialize(json, VKApiUser::class.java)
        val dto = ChatUserDto()
        if (checkObject(json)) {
            val root = json.asJsonObject
            dto.invited_by = optInt(root, "invited_by")
            dto.type = optString(root, "type")
        }
        dto.user = user
        return dto
    }
}