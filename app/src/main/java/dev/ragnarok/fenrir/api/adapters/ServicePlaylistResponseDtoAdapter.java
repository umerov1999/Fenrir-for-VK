package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse;

public class ServicePlaylistResponseDtoAdapter extends AbsAdapter implements JsonDeserializer<ServicePlaylistResponse> {
    private static final String TAG = ServicePlaylistResponseDtoAdapter.class.getSimpleName();

    @Override
    public ServicePlaylistResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        JsonObject root = json.getAsJsonObject();
        ServicePlaylistResponse dto = new ServicePlaylistResponse();
        dto.playlists = new ArrayList<>();
        if (checkArray(root.get("response"))) {
            JsonArray response = root.getAsJsonArray("response");
            for (JsonElement i : response) {
                if (checkObject(i)) {
                    dto.playlists.add(context.deserialize(i, VKApiAudioPlaylist.class));
                }
            }
        } else if (checkObject(root.get("response"))) {
            JsonObject response = root.getAsJsonObject("response");
            dto.playlists.add(context.deserialize(response, VKApiAudioPlaylist.class));
        }
        return dto;
    }
}
