package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiCatalogLink;


public class VKApiCatalogLinkDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiCatalogLink> {
    private static final String TAG = VKApiCatalogLinkDtoAdapter.class.getSimpleName();

    @Override
    public VKApiCatalogLink deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiCatalogLink dto = new VKApiCatalogLink();
        JsonObject root = json.getAsJsonObject();
        dto.url = optString(root, "url");
        dto.title = optString(root, "title");
        dto.subtitle = optString(root, "subtitle");
        if (hasArray(root, "image")) {
            JsonArray arr = root.getAsJsonArray("image");
            int max_res = 0;
            for (JsonElement i : arr) {
                if (!checkObject(i)) {
                    continue;
                }
                JsonObject res = i.getAsJsonObject();
                int curr_res = optInt(res, "height") * optInt(res, "width");
                if (curr_res > max_res) {
                    max_res = curr_res;
                    dto.preview_photo = optString(res, "url");
                }
            }
        }
        return dto;
    }
}
