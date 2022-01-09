package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.api.util.VKStringUtils;

public class MessageDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiMessage> {
    private static final String TAG = MessageDtoAdapter.class.getSimpleName();

    @Override
    public VKApiMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiMessage dto = new VKApiMessage();
        JsonObject root = json.getAsJsonObject();

        dto.id = optInt(root, "id");
        dto.out = optBoolean(root, "out");
        dto.peer_id = optInt(root, "peer_id");
        dto.from_id = root.has("from_id") ? optInt(root, "from_id") : optInt(root, "user_id");

        dto.date = optLong(root, "date");
        //dto.read_state = optBoolean(root, "read_state");
        //dto.title = VKStringUtils.unescape(optString(root, "title"));
        dto.body = VKStringUtils.unescape(root.has("text") ? optString(root, "text") : optString(root, "body"));

        if (hasObject(root, "keyboard")) {
            dto.keyboard = context.deserialize(root.get("keyboard"), VkApiConversation.CurrentKeyboard.class);
        }

        if (hasArray(root, "attachments")) {
            dto.attachments = context.deserialize(root.get("attachments"), VkApiAttachments.class);
        }

        if (hasArray(root, "fwd_messages")) {
            JsonArray fwdArray = root.getAsJsonArray("fwd_messages");
            dto.fwd_messages = new ArrayList<>(fwdArray.size());

            for (int i = 0; i < fwdArray.size(); i++) {
                if (!checkObject(fwdArray.get(i))) {
                    continue;
                }
                dto.fwd_messages.add(deserialize(fwdArray.get(i), VKApiMessage.class, context));
            }
        }

        if (hasObject(root, "reply_message")) {
            if (dto.fwd_messages == null) {
                dto.fwd_messages = new ArrayList<>(1);
            }
            dto.fwd_messages.add(deserialize(root.get("reply_message"), VKApiMessage.class, context));
        }

        dto.deleted = optBoolean(root, "deleted");
        dto.important = optBoolean(root, "important");
        dto.random_id = optString(root, "random_id");
        dto.payload = optString(root, "payload");
        dto.update_time = optLong(root, "update_time");
        dto.conversation_message_id = optInt(root, "conversation_message_id");

        JsonElement actionJson = root.get("action");
        if (checkObject(actionJson)) {
            dto.action = optString(actionJson.getAsJsonObject(), "type");
            dto.action_mid = optInt(actionJson.getAsJsonObject(), "member_id");
            dto.action_text = optString(actionJson.getAsJsonObject(), "text");
            dto.action_email = optString(actionJson.getAsJsonObject(), "email");

            if (hasObject(actionJson.getAsJsonObject(), "photo")) {
                JsonObject photoJson = actionJson.getAsJsonObject().getAsJsonObject("photo");
                dto.action_photo_50 = optString(photoJson, "photo_50");
                dto.action_photo_100 = optString(photoJson, "photo_100");
                dto.action_photo_200 = optString(photoJson, "photo_200");
            }
        }

        return dto;
    }
}