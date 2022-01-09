package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VkApiDoc;

public class DocsEntryDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiDoc.Entry> {

    @Override
    public VkApiDoc.Entry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject o = json.getAsJsonObject();

        String type = optString(o, "type");
        VkApiDoc.Entry entry;
        VkApiDoc pp = context.deserialize(o.get(type), VkApiDoc.class);
        entry = new VkApiDoc.Entry(type, pp);

        return entry;
    }
}
