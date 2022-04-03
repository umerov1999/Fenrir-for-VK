package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VkApiDoc
import java.lang.reflect.Type

class DocsEntryDtoAdapter : AbsAdapter(), JsonDeserializer<VkApiDoc.Entry> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VkApiDoc.Entry {
        val o = json.asJsonObject
        val type = optString(o, "type")
        val entry: VkApiDoc.Entry
        val pp: VkApiDoc = context.deserialize(o[type], VkApiDoc::class.java)
        entry = VkApiDoc.Entry(type, pp)
        return entry
    }
}