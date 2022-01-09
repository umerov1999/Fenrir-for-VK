package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.ChatUserDto;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class ChatUserDtoAdapter extends AbsAdapter implements JsonDeserializer<ChatUserDto> {

    @Override
    public ChatUserDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        VKApiUser user = context.deserialize(json, VKApiUser.class);

        ChatUserDto dto = new ChatUserDto();
        if (checkObject(json)) {
            JsonObject root = json.getAsJsonObject();
            dto.invited_by = optInt(root, "invited_by");
            dto.type = optString(root, "type");
        }

        dto.user = user;
        return dto;
    }
}
