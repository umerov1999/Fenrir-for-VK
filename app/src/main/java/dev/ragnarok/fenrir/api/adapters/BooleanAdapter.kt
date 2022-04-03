package dev.ragnarok.fenrir.api.adapters

import com.google.gson.*
import java.lang.reflect.Type

class BooleanAdapter : AbsAdapter(), JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
    override fun serialize(
        src: Boolean,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(if (src) 1 else 0)
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        src: JsonElement, srcType: Type,
        context: JsonDeserializationContext
    ): Boolean {
        if (!checkPrimitive(src)) {
            return false
        }
        val prim = src.asJsonPrimitive
        return try {
            prim.isBoolean && prim.asBoolean || src.asInt == 1
        } catch (e: Exception) {
            src.asBoolean
        }
    }
}