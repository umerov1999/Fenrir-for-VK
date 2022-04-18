package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.adapters.AttachmentsDtoAdapter.Companion.parse
import dev.ragnarok.fenrir.api.model.VKApiAttachment
import dev.ragnarok.fenrir.api.model.VKApiAttachments
import java.lang.reflect.Type

class AttachmentsEntryDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiAttachments.Entry?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiAttachments.Entry? {
        if (!checkObject(json)) {
            return null
        }
        val o = json.asJsonObject
        val type = optString(o, "type") ?: return null
        val attachment: VKApiAttachment? = try {
            parse(type, o, context)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        var entry: VKApiAttachments.Entry? = null
        if (attachment != null) {
            entry = VKApiAttachments.Entry(type, attachment)
        }
        return entry
    }
}