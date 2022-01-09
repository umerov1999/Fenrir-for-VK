package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;

import dev.ragnarok.fenrir.api.model.VKApiAudio;

public class AudioDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiAudio> {
    private static final String TAG = AudioDtoAdapter.class.getSimpleName();

    @Override
    public VKApiAudio deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiAudio dto = new VKApiAudio();
        JsonObject root = json.getAsJsonObject();
        dto.id = optInt(root, "id");
        dto.owner_id = optInt(root, "owner_id");
        dto.artist = optString(root, "artist");
        dto.title = optString(root, "title");
        dto.duration = optInt(root, "duration");
        dto.url = optString(root, "url");
        dto.lyrics_id = optInt(root, "lyrics_id");
        dto.genre_id = optInt(root, "genre_id");
        dto.access_key = optString(root, "access_key");
        dto.isHq = optBoolean(root, "is_hq");
        if (hasArray(root, "main_artists")) {
            JsonArray arr = root.getAsJsonArray("main_artists");
            dto.main_artists = new HashMap<>(arr.size());
            for (JsonElement i : arr) {
                if (!checkObject(i)) {
                    continue;
                }
                JsonObject artist = i.getAsJsonObject();
                String name = optString(artist, "name");
                String id = optString(artist, "id");
                dto.main_artists.put(id, name);
            }
        }

        if (hasObject(root, "album")) {
            JsonObject thmb = root.getAsJsonObject("album");
            dto.album_id = optInt(thmb, "id");
            dto.album_owner_id = optInt(thmb, "owner_id");
            dto.album_access_key = optString(thmb, "access_key");
            dto.album_title = optString(thmb, "title");

            if (hasObject(thmb, "thumb")) {
                thmb = thmb.getAsJsonObject("thumb");
                if (thmb.has("photo_135"))
                    dto.thumb_image_little = optString(thmb, "photo_135");
                else if (thmb.has("photo_68"))
                    dto.thumb_image_little = optString(thmb, "photo_68");
                else if (thmb.has("photo_34"))
                    dto.thumb_image_little = optString(thmb, "photo_34");

                dto.thumb_image_very_big = optString(thmb, "photo_1200");
                if (thmb.has("photo_600")) {
                    dto.thumb_image_big = optString(thmb, "photo_600");
                    if (dto.thumb_image_very_big == null)
                        dto.thumb_image_very_big = optString(thmb, "photo_600");
                } else if (thmb.has("photo_300")) {
                    dto.thumb_image_big = optString(thmb, "photo_300");
                    if (dto.thumb_image_very_big == null)
                        dto.thumb_image_very_big = optString(thmb, "photo_300");
                }
            }
        }

        return dto;
    }
}
