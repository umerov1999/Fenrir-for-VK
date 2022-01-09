package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiChat;
import dev.ragnarok.fenrir.api.model.response.ChatsInfoResponse;

public class ChatsInfoAdapter extends AbsAdapter implements JsonDeserializer<ChatsInfoResponse> {
    @Override
    public ChatsInfoResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<VKApiChat> chats;

        if (checkObject(json)) {
            chats = Collections.singletonList(context.deserialize(json, VKApiChat.class));
        } else if (checkArray(json)) {
            JsonArray array = json.getAsJsonArray();
            chats = parseArray(array, VKApiChat.class, context, Collections.emptyList());
        } else {
            chats = Collections.emptyList();
        }

        ChatsInfoResponse response = new ChatsInfoResponse();
        response.chats = chats;
        return response;
    }
}
