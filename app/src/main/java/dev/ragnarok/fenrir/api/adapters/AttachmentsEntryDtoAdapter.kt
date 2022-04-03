package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.adapters.AttachmentsDtoAdapter.Companion.parse
import dev.ragnarok.fenrir.api.model.VKApiAttachment
import dev.ragnarok.fenrir.api.model.VkApiAttachments
import java.lang.reflect.Type

class AttachmentsEntryDtoAdapter : AbsAdapter(), JsonDeserializer<VkApiAttachments.Entry?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VkApiAttachments.Entry? {
        if (!checkObject(json)) {
            return null
        }
        val o = json.asJsonObject
        val type = optString(o, "type")
        val attachment: VKApiAttachment? = try {
            parse(type, o, context)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        var entry: VkApiAttachments.Entry? = null
        if (attachment != null) {
            entry = VkApiAttachments.Entry(type, attachment)
        }
        return entry
    }
}