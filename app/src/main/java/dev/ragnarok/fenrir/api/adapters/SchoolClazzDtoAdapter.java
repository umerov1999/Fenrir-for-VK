package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.database.SchoolClazzDto;

public class SchoolClazzDtoAdapter extends AbsAdapter implements JsonDeserializer<SchoolClazzDto> {
    private static final String TAG = SchoolClazzDtoAdapter.class.getSimpleName();

    @Override
    public SchoolClazzDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkArray(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        SchoolClazzDto dto = new SchoolClazzDto();
        JsonArray root = json.getAsJsonArray();
        dto.id = optInt(root, 0);

        if (root.get(1) instanceof JsonPrimitive) {
            JsonPrimitive second = root.get(1).getAsJsonPrimitive();
            if (second.isString()) {
                dto.title = second.getAsString();
            } else if (second.isNumber()) {
                dto.title = String.valueOf(second.getAsNumber());
            }
        }

        return dto;
    }
}
