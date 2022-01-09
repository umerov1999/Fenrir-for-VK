package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.FaveLinkDto;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;

public class FaveLinkDtoAdapter extends AbsAdapter implements JsonDeserializer<FaveLinkDto> {
    private static final String TAG = FaveLinkDtoAdapter.class.getSimpleName();

    @Override
    public FaveLinkDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        FaveLinkDto link = new FaveLinkDto();
        JsonObject root = json.getAsJsonObject();

        if (!hasObject(root, "link"))
            return link;
        root = root.get("link").getAsJsonObject();
        link.id = optString(root, "id");
        link.description = optString(root, "description");
        if (hasObject(root, "photo")) {
            link.photo = context.deserialize(root.get("photo"), VKApiPhoto.class);
        }
        link.title = optString(root, "title");
        link.url = optString(root, "url");

        return link;
    }
}
