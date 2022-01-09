package dev.ragnarok.fenrir.api.adapters;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.CommentsDto;
import dev.ragnarok.fenrir.api.model.VKApiTopic;

public class TopicDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiTopic> {
    private static final String TAG = TopicDtoAdapter.class.getSimpleName();

    @Override
    public VKApiTopic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiTopic dto = new VKApiTopic();
        JsonObject root = json.getAsJsonObject();

        dto.id = optInt(root, "id");
        dto.owner_id = optInt(root, "owner_id");
        dto.title = optString(root, "title");
        dto.created = optLong(root, "created");
        dto.created_by = optInt(root, "created_by");
        dto.updated = optInt(root, "updated");
        dto.updated_by = optInt(root, "updated_by");
        dto.is_closed = optBoolean(root, "is_closed");
        dto.is_fixed = optBoolean(root, "is_fixed");

        JsonElement commentsJson = root.get("comments");
        if (nonNull(commentsJson)) {
            if (commentsJson.isJsonObject()) {
                dto.comments = context.deserialize(commentsJson, CommentsDto.class);
            } else {
                dto.comments = new CommentsDto();
                dto.comments.count = commentsJson.getAsInt();
            }
        }

        dto.first_comment = optString(root, "first_comment");
        dto.last_comment = optString(root, "last_comment");
        return dto;
    }
}