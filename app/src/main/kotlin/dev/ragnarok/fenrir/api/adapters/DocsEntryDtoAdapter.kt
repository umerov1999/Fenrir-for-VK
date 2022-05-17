package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiDoc
import java.lang.reflect.Type

class DocsEntryDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiDoc.Entry> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiDoc.Entry {
        val o = json.asJsonObject
        val type = optString(o, "type") ?: throw NullPointerException("VKApiDoc.Entry type")
        val entry: VKApiDoc.Entry
        val pp: VKApiDoc = context.deserialize(o[type], VKApiDoc::class.java)
        entry = VKApiDoc.Entry(type, pp)
        return entry
    }
}