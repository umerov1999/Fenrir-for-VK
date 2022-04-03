package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.VkApiAttachments
import dev.ragnarok.fenrir.api.model.VkApiConversation.CurrentKeyboard
import dev.ragnarok.fenrir.api.util.VKStringUtils
import java.lang.reflect.Type

class MessageDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiMessage> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiMessage {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiMessage()
        val root = json.asJsonObject
        dto.id = optInt(root, "id")
        dto.out = optBoolean(root, "out")
        dto.peer_id = optInt(root, "peer_id")
        dto.from_id = if (root.has("from_id")) optInt(root, "from_id") else optInt(root, "user_id")
        dto.date = optLong(root, "date")
        //dto.read_state = optBoolean(root, "read_state");
        //dto.title = VKStringUtils.unescape(optString(root, "title"));
        dto.body = VKStringUtils.unescape(
            if (root.has("text")) optString(
                root,
                "text"
            ) else optString(root, "body")
        )
        if (hasObject(root, "keyboard")) {
            dto.keyboard = context.deserialize(root["keyboard"], CurrentKeyboard::class.java)
        }
        if (hasArray(root, "attachments")) {
            dto.attachments = context.deserialize(root["attachments"], VkApiAttachments::class.java)
        }
        if (hasArray(root, "fwd_messages")) {
            val fwdArray = root.getAsJsonArray("fwd_messages")
            dto.fwd_messages = ArrayList(fwdArray.size())
            for (i in 0 until fwdArray.size()) {
                if (!checkObject(fwdArray[i])) {
                    continue
                }
                dto.fwd_messages.add(deserialize(fwdArray[i], VKApiMessage::class.java, context))
            }
        }
        if (hasObject(root, "reply_message")) {
            if (dto.fwd_messages == null) {
                dto.fwd_messages = ArrayList(1)
            }
            dto.fwd_messages.add(
                deserialize(
                    root["reply_message"],
                    VKApiMessage::class.java,
                    context
                )
            )
        }
        dto.deleted = optBoolean(root, "deleted")
        dto.important = optBoolean(root, "important")
        dto.random_id = optString(root, "random_id")
        dto.payload = optString(root, "payload")
        dto.update_time = optLong(root, "update_time")
        dto.conversation_message_id = optInt(root, "conversation_message_id")
        val actionJson = root["action"]
        if (checkObject(actionJson)) {
            dto.action = optString(actionJson.asJsonObject, "type")
            dto.action_mid = optInt(actionJson.asJsonObject, "member_id")
            dto.action_text = optString(actionJson.asJsonObject, "text")
            dto.action_email = optString(actionJson.asJsonObject, "email")
            if (hasObject(actionJson.asJsonObject, "photo")) {
                val photoJson = actionJson.asJsonObject.getAsJsonObject("photo")
                dto.action_photo_50 = optString(photoJson, "photo_50")
                dto.action_photo_100 = optString(photoJson, "photo_100")
                dto.action_photo_200 = optString(photoJson, "photo_200")
            }
        }
        return dto
    }

    companion object {
        private val TAG = MessageDtoAdapter::class.java.simpleName
    }
}