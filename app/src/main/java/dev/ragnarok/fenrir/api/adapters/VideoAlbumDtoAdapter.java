package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiVideoAlbum;
import dev.ragnarok.fenrir.api.model.VkApiPrivacy;

public class VideoAlbumDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiVideoAlbum> {
    private static final String TAG = VideoAlbumDtoAdapter.class.getSimpleName();

    @Override
    public VKApiVideoAlbum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiVideoAlbum album = new VKApiVideoAlbum();
        JsonObject root = json.getAsJsonObject();

        album.id = optInt(root, "id");
        album.owner_id = optInt(root, "owner_id");
        album.title = optString(root, "title");
        album.count = optInt(root, "count");
        album.updated_time = optInt(root, "updated_time");
        if (hasObject(root, "privacy_view")) {
            album.privacy = context.deserialize(root.get("privacy_view"), VkApiPrivacy.class);
        }
        if (hasArray(root, "image")) {
            JsonArray images = root.getAsJsonArray("image");
            for (int i = 0; i < images.size(); i++) {
                if (!checkObject(images.get(i))) {
                    continue;
                }
                if (images.get(i).getAsJsonObject().get("width").getAsInt() >= 800) {
                    album.image = images.get(i).getAsJsonObject().get("url").getAsString();
                    break;
                }
            }
            if (album.image == null) {
                if (checkObject(images.get(images.size() - 1))) {
                    album.image = images.get(images.size() - 1).getAsJsonObject().get("url").getAsString();
                }
            }
        } else if (root.has("photo_800")) {
            album.image = optString(root, "photo_800");
        } else if (root.has("photo_320")) {
            album.image = optString(root, "photo_320");
        }
        return album;
    }
}
