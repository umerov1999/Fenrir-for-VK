package dev.ragnarok.fenrir.api.adapters;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates;
import dev.ragnarok.fenrir.util.Logger;

public class LongpollUpdatesAdapter extends AbsAdapter implements JsonDeserializer<VkApiLongpollUpdates> {

    private static final String TAG = LongpollUpdatesAdapter.class.getSimpleName();

    @Override
    public VkApiLongpollUpdates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        VkApiLongpollUpdates updates = new VkApiLongpollUpdates();
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        JsonObject root = json.getAsJsonObject();

        updates.failed = optInt(root, "failed");
        updates.ts = optLong(root, "ts");

        JsonElement array = root.get("updates");

        if (checkArray(array)) {
            for (int i = 0; i < array.getAsJsonArray().size(); i++) {
                JsonArray updateArray = array.getAsJsonArray().get(i).getAsJsonArray();

                AbsLongpollEvent event = context.deserialize(updateArray, AbsLongpollEvent.class);

                if (nonNull(event)) {
                    updates.putUpdate(event);
                } else {
                    Logger.d(TAG, "Unhandled Longpoll event: array: " + updateArray);
                }
            }
        }

        return updates;
    }
}
