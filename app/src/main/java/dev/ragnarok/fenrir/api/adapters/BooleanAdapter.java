package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class BooleanAdapter extends AbsAdapter implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {

    @Override
    public JsonElement serialize(Boolean src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src ? 1 : 0);
    }

    @Override
    public Boolean deserialize(JsonElement src, Type srcType,
                               JsonDeserializationContext context) throws JsonParseException {
        if (!checkPrimitive(src)) {
            return false;
        }
        JsonPrimitive prim = src.getAsJsonPrimitive();
        try {
            return prim.isBoolean() && prim.getAsBoolean() || src.getAsInt() == 1;
        } catch (Exception e) {
            return src.getAsBoolean();
        }
    }
}