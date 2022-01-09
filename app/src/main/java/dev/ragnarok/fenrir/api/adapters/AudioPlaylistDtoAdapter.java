package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;

public class AudioPlaylistDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiAudioPlaylist> {
    private static final String TAG = AudioPlaylistDtoAdapter.class.getSimpleName();

    @Override
    public VKApiAudioPlaylist deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiAudioPlaylist album = new VKApiAudioPlaylist();
        JsonObject root = json.getAsJsonObject();

        album.id = optInt(root, "id");
        album.count = optInt(root, "count");
        album.owner_id = optInt(root, "owner_id");
        album.title = optString(root, "title");
        album.access_key = optString(root, "access_key");
        album.description = optString(root, "description");
        album.update_time = optInt(root, "update_time");
        album.Year = optInt(root, "year");

        if (hasArray(root, "genres")) {
            StringBuilder build = new StringBuilder();
            JsonArray gnr = root.getAsJsonArray("genres");
            boolean isFirst = true;
            for (JsonElement i : gnr) {
                if (!checkObject(i)) {
                    continue;
                }
                if (isFirst)
                    isFirst = false;
                else
                    build.append(", ");
                String val = optString(i.getAsJsonObject(), "name");
                if (val != null)
                    build.append(val);
            }
            album.genre = build.toString();
        }

        if (hasObject(root, "original")) {
            JsonObject orig = root.getAsJsonObject("original");
            album.original_id = optInt(orig, "playlist_id");
            album.original_owner_id = optInt(orig, "owner_id");
            album.original_access_key = optString(orig, "access_key");
        }

        if (hasArray(root, "main_artists")) {
            JsonElement artist = root.getAsJsonArray("main_artists").get(0);
            if (checkObject(artist)) {
                album.artist_name = optString(artist.getAsJsonObject(), "name");
            }
        }
        if (hasObject(root, "photo")) {
            JsonObject thmb = root.getAsJsonObject("photo");

            if (thmb.has("photo_600"))
                album.thumb_image = optString(thmb, "photo_600");
            else if (thmb.has("photo_300"))
                album.thumb_image = optString(thmb, "photo_300");
        } else if (hasArray(root, "thumbs")) {
            JsonElement thmbc = root.getAsJsonArray("thumbs").get(0);
            if (checkObject(thmbc)) {
                if (thmbc.getAsJsonObject().has("photo_600"))
                    album.thumb_image = optString(thmbc.getAsJsonObject(), "photo_600");
                else if (thmbc.getAsJsonObject().has("photo_300"))
                    album.thumb_image = optString(thmbc.getAsJsonObject(), "photo_300");
            }
        }
        return album;
    }
}
