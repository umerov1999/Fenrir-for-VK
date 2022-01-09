package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiNews;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPlace;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;

public class NewsAdapter extends AbsAdapter implements JsonDeserializer<VKApiNews> {
    private static final String TAG = NewsAdapter.class.getSimpleName();

    @Override
    public VKApiNews deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiNews dto = new VKApiNews();
        JsonObject root = json.getAsJsonObject();

        dto.type = optString(root, "type");
        dto.source_id = optInt(root, "source_id");
        dto.date = optLong(root, "date");
        dto.post_id = optInt(root, "post_id");
        dto.post_type = optString(root, "post_type");
        dto.final_post = optBoolean(root, "final_post");
        dto.copy_owner_id = optInt(root, "copy_owner_id");
        dto.copy_post_id = optInt(root, "copy_post_id");
        dto.mark_as_ads = optInt(root, "mark_as_ads");

        if (hasArray(root, "copy_history")) {
            dto.copy_history = parseArray(root.getAsJsonArray("copy_history"), VKApiPost.class, context, Collections.emptyList());
        } else {
            dto.copy_history = Collections.emptyList();
        }

        dto.copy_post_date = optLong(root, "copy_post_date");
        dto.text = optString(root, "text");
        dto.can_edit = optBoolean(root, "can_edit");
        dto.can_delete = optBoolean(root, "can_delete");

        if (hasObject(root, "comments")) {
            JsonObject commentsRoot = root.getAsJsonObject("comments");
            dto.comment_count = optInt(commentsRoot, "count");
            dto.comment_can_post = optBoolean(commentsRoot, "can_post");
        }

        if (hasObject(root, "likes")) {
            JsonObject likesRoot = root.getAsJsonObject("likes");
            dto.like_count = optInt(likesRoot, "count");
            dto.user_like = optBoolean(likesRoot, "user_likes");
            dto.can_like = optBoolean(likesRoot, "can_like");
            dto.can_publish = optBoolean(likesRoot, "can_publish");
        }

        if (hasObject(root, "reposts")) {
            JsonObject repostsRoot = root.getAsJsonObject("reposts");
            dto.reposts_count = optInt(repostsRoot, "count");
            dto.user_reposted = optBoolean(repostsRoot, "user_reposted");
        }

        if (hasObject(root, "views")) {
            JsonObject viewRoot = root.getAsJsonObject("views");
            dto.views = optInt(viewRoot, "count", 0);
        }

        if (hasArray(root, "attachments")) {
            dto.attachments = context.deserialize(root.get("attachments"), VkApiAttachments.class);
        }

        if (root.has("geo")) {
            dto.geo = context.deserialize(root.get("geo"), VKApiPlace.class);
        }

        if (root.has("photos")) {
            JsonArray photosArray = root.getAsJsonObject("photos").getAsJsonArray("items");
            if (dto.attachments == null) {
                dto.attachments = new VkApiAttachments();
            }
            dto.attachments.append(parseArray(photosArray, VKApiPhoto.class, context, null));
        }

        if (root.has("photo_tags")) {
            JsonArray photosTagsArray = root.getAsJsonObject("photo_tags").getAsJsonArray("items");
            if (dto.attachments == null) {
                dto.attachments = new VkApiAttachments();
            }
            dto.attachments.append(parseArray(photosTagsArray, VKApiPhoto.class, context, null));
        }

        if (root.has("audio")) {
            JsonArray photosTagsArray = root.getAsJsonObject("audio").getAsJsonArray("items");
            if (dto.attachments == null) {
                dto.attachments = new VkApiAttachments();
            }
            dto.attachments.append(parseArray(photosTagsArray, VKApiAudio.class, context, null));
        }

        if (root.has("video")) {
            JsonArray photosTagsArray = root.getAsJsonObject("video").getAsJsonArray("items");
            if (dto.attachments == null) {
                dto.attachments = new VkApiAttachments();
            }
            dto.attachments.append(parseArray(photosTagsArray, VKApiVideo.class, context, null));
        }

        if (root.has("friends")) {
            JsonArray friendsArray = root.getAsJsonObject("friends").getAsJsonArray("items");
            dto.friends = new ArrayList<>(friendsArray.size());
            for (int i = 0; i < friendsArray.size(); i++) {
                JsonObject friendObj = friendsArray.get(i).getAsJsonObject();
                dto.friends.add(friendObj.get("user_id").getAsInt());
            }
        }

        return dto;
    }
}