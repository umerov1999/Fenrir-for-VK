package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VkApiProfileInfoResponce;

public class ProfileInfoResponceDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiProfileInfoResponce> {
    private static final String TAG = ProfileInfoResponceDtoAdapter.class.getSimpleName();

    @Override
    public VkApiProfileInfoResponce deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VkApiProfileInfoResponce info = new VkApiProfileInfoResponce();
        JsonObject root = json.getAsJsonObject();

        if (root.has("name_request")) {
            info.status = 2;
        } else {
            info.status = optInt(root, "changed", 0) == 1 ? 1 : 0;
        }
        return info;
    }
}
