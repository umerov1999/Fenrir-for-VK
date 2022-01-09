package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.CommentsDto;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.response.NewsfeedCommentsResponse;

public class NewsfeedCommentDtoAdapter extends AbsAdapter implements JsonDeserializer<NewsfeedCommentsResponse.Dto> {

    @Override
    public NewsfeedCommentsResponse.Dto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        NewsfeedCommentsResponse.Dto dto = null;
        if (!checkObject(json)) {
            return null;
        }
        JsonObject root = json.getAsJsonObject();
        String type = optString(root, "type", "post");

        if ("photo".equals(type)) {
            dto = new NewsfeedCommentsResponse.PhotoDto(context.deserialize(root, VKApiPhoto.class));
        } else if ("post".equals(type)) {
            dto = new NewsfeedCommentsResponse.PostDto(context.deserialize(root, VKApiPost.class));
        } else if ("video".equals(type)) {
            dto = new NewsfeedCommentsResponse.VideoDto(context.deserialize(root, VKApiVideo.class));
        } else if ("topic".equals(type)) {
            VKApiTopic topic = new VKApiTopic();
            topic.id = optInt(root, "post_id");
            if (root.has("to_id"))
                topic.owner_id = optInt(root, "to_id");
            else
                topic.owner_id = optInt(root, "source_id");
            topic.title = optString(root, "text");
            topic.comments = context.deserialize(root.get("comments"), CommentsDto.class);
            dto = new NewsfeedCommentsResponse.TopicDto(topic);
        }

        return dto;
    }
}