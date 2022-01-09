package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VkApiPostSource;

public class PostSourceDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiPostSource> {

    @Override
    public VkApiPostSource deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VkApiPostSource dto = new VkApiPostSource();
        dto.type = VkApiPostSource.Type.parse(optString(root, "type"));
        dto.platform = optString(root, "platform");
        dto.data = VkApiPostSource.Data.parse(optString(root, "data"));
        dto.url = optString(root, "url");

        return dto;
    }
}
