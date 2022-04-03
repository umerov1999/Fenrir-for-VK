package dev.ragnarok.fenrir.db.serialize

import com.google.gson.*
import dev.ragnarok.fenrir.db.model.AttachmentsTypes
import dev.ragnarok.fenrir.db.model.entity.Entity
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper
import java.lang.reflect.Type

class EntityWrapperAdapter : JsonSerializer<EntityWrapper?>, JsonDeserializer<EntityWrapper?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        typef: Type,
        context: JsonDeserializationContext
    ): EntityWrapper? {
        if (jsonElement !is JsonObject) {
            return null
        }
        val root = jsonElement.getAsJsonObject()
        val isNull = root[KEY_IS_NULL].asBoolean
        var entity: Entity? = null
        if (!isNull) {
            val dbotype = root[KEY_TYPE].asInt
            entity = context.deserialize(root[KEY_ENTITY], AttachmentsTypes.classForType(dbotype))
        }
        return EntityWrapper().wrap(entity)
    }

    override fun serialize(
        wrapper: EntityWrapper?,
        type: Type,
        context: JsonSerializationContext
    ): JsonElement {
        if (wrapper == null) {
            return JsonNull.INSTANCE
        }
        val root = JsonObject()
        val entity = wrapper.get()
        root.add(KEY_IS_NULL, JsonPrimitive(entity == null))
        if (entity != null) {
            root.add(KEY_TYPE, JsonPrimitive(AttachmentsTypes.typeForInstance(entity)))
            root.add(KEY_ENTITY, context.serialize(entity))
        }
        return root
    }

    companion object {
        private const val KEY_IS_NULL = "is_null"
        private const val KEY_ENTITY = "entity"
        private const val KEY_TYPE = "type"
    }
}