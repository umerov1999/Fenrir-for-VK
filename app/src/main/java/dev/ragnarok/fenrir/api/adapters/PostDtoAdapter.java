package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import dev.ragnarok.fenrir.api.model.CommentsDto;
import dev.ragnarok.fenrir.api.model.VKApiPlace;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.VkApiPostSource;
import dev.ragnarok.fenrir.util.Utils;

public class PostDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiPost> {
    private static final String TAG = PostDtoAdapter.class.getSimpleName();

    @Override
    public VKApiPost deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiPost dto = new VKApiPost();
        JsonObject root = json.getAsJsonObject();

        dto.id = getFirstInt(root, 0, "post_id", "id");
        dto.post_type = VKApiPost.Type.parse(optString(root, "post_type"));
        dto.owner_id = getFirstInt(root, 0, "owner_id", "to_id", "source_id");

        dto.from_id = optInt(root, "from_id");

        if (dto.from_id == 0) {
            // "copy_history": [
            // {
            //     ... this post has been removed ...
            //     "id": 1032,
            //     "owner_id": 216143660,
            //     "from_id": 0,
            //     "date": 0,
            //     "post_type": "post",
            dto.from_id = dto.owner_id;
        }

        dto.date = optLong(root, "date");
        dto.text = optString(root, "text");
        if (hasObject(root, "copyright")) {
            JsonObject cop = root.getAsJsonObject("copyright");
            if (Utils.isEmpty(dto.text)) {
                dto.text = "";
            }
            String name = optString(cop, "name");
            String link = optString(cop, "link");
            dto.text = ("[" + link + "|©" + name + "]\r\n") + dto.text;
        }
        dto.reply_owner_id = optInt(root, "reply_owner_id", 0);

        if (dto.reply_owner_id == 0) {
            // for replies from newsfeed.search
            // но не помешало бы понять какого хе...а!!!
            dto.reply_owner_id = dto.owner_id;
        }

        dto.reply_post_id = optInt(root, "reply_post_id", 0);
        if (dto.reply_post_id == 0) {
            // for replies from newsfeed.search
            // но не помешало бы понять какого хе...а (1)!!!
            dto.reply_post_id = optInt(root, "post_id");
        }

        dto.friends_only = optBoolean(root, "friends_only");

        if (hasObject(root, "comments")) {
            dto.comments = context.deserialize(root.get("comments"), CommentsDto.class);
        }

        if (hasObject(root, "likes")) {
            JsonObject likes = root.getAsJsonObject("likes");
            dto.likes_count = optInt(likes, "count");
            dto.user_likes = optBoolean(likes, "user_likes");
            dto.can_like = optBoolean(likes, "can_like");
            dto.can_publish = optBoolean(likes, "can_publish");
        }

        if (hasObject(root, "reposts")) {
            JsonObject reposts = root.getAsJsonObject("reposts");
            dto.reposts_count = optInt(reposts, "count");
            dto.user_reposted = optBoolean(reposts, "user_reposted");
        }

        if (hasObject(root, "views")) {
            JsonObject views = root.getAsJsonObject("views");
            dto.views = optInt(views, "count");
        }

        if (hasArray(root, "attachments")) {
            dto.attachments = context.deserialize(root.get("attachments"), VkApiAttachments.class);
        }

        if (hasObject(root, "geo")) {
            dto.geo = context.deserialize(root.get("geo"), VKApiPlace.class);
        }

        dto.can_edit = optBoolean(root, "can_edit");

        dto.signer_id = optInt(root, "signer_id");
        dto.created_by = optInt(root, "created_by");
        dto.can_pin = optInt(root, "can_pin") == 1;
        dto.is_pinned = optBoolean(root, "is_pinned");

        if (hasArray(root, "copy_history")) {
            JsonArray copyHistoryArray = root.getAsJsonArray("copy_history");
            dto.copy_history = new ArrayList<>(copyHistoryArray.size());

            for (int i = 0; i < copyHistoryArray.size(); i++) {
                if (!checkObject(copyHistoryArray.get(i))) {
                    continue;
                }
                JsonObject copy = copyHistoryArray.get(i).getAsJsonObject();
                dto.copy_history.add(deserialize(copy, VKApiPost.class, context));
            }

        } else {
            //empty list
            dto.copy_history = new ArrayList<>(0);
        }

        if (hasObject(root, "post_source")) {
            dto.post_source = context.deserialize(root.get("post_source"), VkApiPostSource.class);
        }
        return dto;
    }
}
