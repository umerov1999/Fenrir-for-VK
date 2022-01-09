package dev.ragnarok.fenrir.db.serialize;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.adapters.AbsAdapter;
import dev.ragnarok.fenrir.db.model.AttachmentsTypes;
import dev.ragnarok.fenrir.db.model.entity.AttachmentsEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;


public class AttachmentsDboAdapter extends AbsAdapter implements JsonDeserializer<AttachmentsEntity>, JsonSerializer<AttachmentsEntity> {

    private static final String KEY_ENTITY = "entity";
    private static final String KEY_ENTITY_TYPE = "dbo_type";

    @Override
    public AttachmentsEntity deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (!checkArray(jsonElement)) {
            return null;
        }
        JsonArray array = jsonElement.getAsJsonArray();
        List<Entity> entities = new ArrayList<>(array.size());

        for (int i = 0; i < array.size(); i++) {
            JsonObject o = array.get(i).getAsJsonObject();
            int dbotype = o.get(KEY_ENTITY_TYPE).getAsInt();
            entities.add(context.deserialize(o.get(KEY_ENTITY), AttachmentsTypes.classForType(dbotype)));
        }

        return AttachmentsEntity.from(entities);
    }

    @Override
    public JsonElement serialize(AttachmentsEntity attachmentsEntity, Type type, JsonSerializationContext context) {
        if (isNull(attachmentsEntity) || attachmentsEntity.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        List<Entity> entities = attachmentsEntity.getEntities();

        JsonArray array = new JsonArray(entities.size());
        for (Entity entity : entities) {
            int dbotype = AttachmentsTypes.typeForInstance(entity);

            JsonObject o = new JsonObject();
            o.add(KEY_ENTITY_TYPE, new JsonPrimitive(dbotype));
            o.add(KEY_ENTITY, context.serialize(entity));

            array.add(o);
        }

        return array;
    }
}