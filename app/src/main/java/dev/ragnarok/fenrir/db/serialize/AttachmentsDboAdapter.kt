package dev.ragnarok.fenrir.db.serialize

import com.google.gson.*
import dev.ragnarok.fenrir.api.adapters.AbsAdapter
import dev.ragnarok.fenrir.db.model.AttachmentsTypes
import dev.ragnarok.fenrir.db.model.entity.AttachmentsEntity
import dev.ragnarok.fenrir.db.model.entity.Entity
import java.lang.reflect.Type

class AttachmentsDboAdapter : AbsAdapter(), JsonDeserializer<AttachmentsEntity?>,
    JsonSerializer<AttachmentsEntity?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): AttachmentsEntity? {
        if (!checkArray(jsonElement)) {
            return null
        }
        val array = jsonElement.asJsonArray
        val entities: MutableList<Entity> = ArrayList(array.size())
        for (i in 0 until array.size()) {
            val o = array[i].asJsonObject
            val dbotype = o[KEY_ENTITY_TYPE].asInt
            entities.add(context.deserialize(o[KEY_ENTITY], AttachmentsTypes.classForType(dbotype)))
        }
        return AttachmentsEntity.from(entities)!!
    }

    override fun serialize(
        attachmentsEntity: AttachmentsEntity?,
        type: Type,
        context: JsonSerializationContext
    ): JsonElement {
        if (attachmentsEntity == null || attachmentsEntity.isEmpty) {
            return JsonNull.INSTANCE
        }
        val entities = attachmentsEntity.entities
        val array = JsonArray(entities.size)
        for (entity in entities) {
            val dbotype = AttachmentsTypes.typeForInstance(entity)
            val o = JsonObject()
            o.add(KEY_ENTITY_TYPE, JsonPrimitive(dbotype))
            o.add(KEY_ENTITY, context.serialize(entity))
            array.add(o)
        }
        return array
    }

    companion object {
        private const val KEY_ENTITY = "entity"
        private const val KEY_ENTITY_TYPE = "dbo_type"
    }
}