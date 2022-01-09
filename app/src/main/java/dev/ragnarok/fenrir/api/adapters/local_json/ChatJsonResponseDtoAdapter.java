package dev.ragnarok.fenrir.api.adapters.local_json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Collections;

import dev.ragnarok.fenrir.api.adapters.AbsAdapter;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.local_json.ChatJsonResponse;

public class ChatJsonResponseDtoAdapter extends AbsAdapter implements JsonDeserializer<ChatJsonResponse> {

    @Override
    public ChatJsonResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ChatJsonResponse story = new ChatJsonResponse();
        if (!checkObject(json)) {
            return story;
        }
        JsonObject root = json.getAsJsonObject();

        story.type = optString(root, "type");
        story.page_avatar = optString(root, "page_avatar");
        story.page_id = optInt(root, "page_id");
        story.page_instagram = optString(root, "page_instagram");
        story.page_phone_number = optString(root, "page_phone_number");
        story.page_site = optString(root, "page_site");
        story.page_title = optString(root, "page_title");
        story.version = context.deserialize(root.get("version"), ChatJsonResponse.Version.class);
        story.messages = parseArray(root.getAsJsonArray(story.type), VKApiMessage.class, context, Collections.emptyList());
        return story;
    }
}
