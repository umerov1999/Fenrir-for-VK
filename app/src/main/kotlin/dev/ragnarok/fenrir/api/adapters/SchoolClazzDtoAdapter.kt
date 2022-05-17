package dev.ragnarok.fenrir.api.adapters

import com.google.gson.*
import dev.ragnarok.fenrir.api.model.database.SchoolClazzDto
import java.lang.reflect.Type

class SchoolClazzDtoAdapter : AbsAdapter(), JsonDeserializer<SchoolClazzDto> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SchoolClazzDto {
        if (!checkArray(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = SchoolClazzDto()
        val root = json.asJsonArray
        dto.id = optInt(root, 0)
        if (root[1] is JsonPrimitive) {
            val second = root[1].asJsonPrimitive
            if (second.isString) {
                dto.title = second.asString
            } else if (second.isNumber) {
                dto.title = second.asNumber.toString()
            }
        }
        return dto
    }

    companion object {
        private val TAG = SchoolClazzDtoAdapter::class.java.simpleName
    }
}