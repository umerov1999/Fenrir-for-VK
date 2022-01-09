package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.response.CustomCommentsResponse;

public class CustomCommentsResponseAdapter extends AbsAdapter implements JsonDeserializer<CustomCommentsResponse> {
    private static final String TAG = CustomCommentsResponseAdapter.class.getSimpleName();

    @Override
    public CustomCommentsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        CustomCommentsResponse response = new CustomCommentsResponse();
        JsonObject root = json.getAsJsonObject();

        JsonElement main = root.get("main");
        if (checkObject(main)) {
            response.main = context.deserialize(main, CustomCommentsResponse.Main.class);
        } // "main": false (if has execute errors)

        if (root.has("first_id")) {
            JsonElement firstIdJson = root.get("first_id");
            response.firstId = firstIdJson.isJsonNull() ? null : firstIdJson.getAsInt();
        }

        if (root.has("last_id")) {
            JsonElement lastIdJson = root.get("last_id");
            response.lastId = lastIdJson.isJsonNull() ? null : lastIdJson.getAsInt();
        }

        if (root.has("admin_level")) {
            JsonElement adminLevelJson = root.get("admin_level");
            response.admin_level = adminLevelJson.isJsonNull() ? null : adminLevelJson.getAsInt();
        }

        return response;
    }
}