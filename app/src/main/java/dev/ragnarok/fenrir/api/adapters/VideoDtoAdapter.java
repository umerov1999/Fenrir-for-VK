package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.CommentsDto;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VkApiPrivacy;
import dev.ragnarok.fenrir.util.Utils;

public class VideoDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiVideo> {
    private static final String TAG = VideoDtoAdapter.class.getSimpleName();

    @Override
    public VKApiVideo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        JsonObject root = json.getAsJsonObject();
        VKApiVideo dto = new VKApiVideo();

        dto.id = optInt(root, "id");
        dto.owner_id = optInt(root, "owner_id");
        dto.title = optString(root, "title");
        dto.description = optString(root, "description");
        dto.duration = optInt(root, "duration");
        dto.link = optString(root, "link");
        dto.date = optLong(root, "date");
        dto.adding_date = optLong(root, "adding_date");
        dto.views = optInt(root, "views");

        if (hasObject(root, "comments")) {
            //for example, newsfeed.getComment
            dto.comments = context.deserialize(root.get("comments"), CommentsDto.class);
        } else {
            // video.get
            dto.comments = new CommentsDto();
            dto.comments.count = optInt(root, "comments", 0);
        }

        dto.player = optString(root, "player");
        dto.access_key = optString(root, "access_key");
        dto.album_id = optInt(root, "album_id");

        if (hasObject(root, "likes")) {
            JsonObject likesRoot = root.getAsJsonObject("likes");
            dto.likes = optInt(likesRoot, "count");
            dto.user_likes = optBoolean(likesRoot, "user_likes");
        }

        dto.can_comment = optBoolean(root, "can_comment");
        dto.can_repost = optBoolean(root, "can_repost");
        dto.repeat = optBoolean(root, "repeat");

        if (hasObject(root, "privacy_view")) {
            dto.privacy_view = context.deserialize(root.get("privacy_view"), VkApiPrivacy.class);
        }

        if (hasObject(root, "privacy_comment")) {
            dto.privacy_comment = context.deserialize(root.get("privacy_comment"), VkApiPrivacy.class);
        }

        if (hasObject(root, "files")) {
            JsonObject filesRoot = root.getAsJsonObject("files");
            dto.mp4_240 = optString(filesRoot, "mp4_240");
            dto.mp4_360 = optString(filesRoot, "mp4_360");
            dto.mp4_480 = optString(filesRoot, "mp4_480");
            dto.mp4_720 = optString(filesRoot, "mp4_720");
            dto.mp4_1080 = optString(filesRoot, "mp4_1080");
            dto.external = optString(filesRoot, "external");
            dto.hls = optString(filesRoot, "hls");
            dto.live = optString(filesRoot, "live");
        }

        int sz = (!Utils.isEmpty(dto.external) && dto.external.contains("youtube")) ? 320 : 800;

        if (hasArray(root, "image")) {
            JsonArray images = root.getAsJsonArray("image");
            if (images.size() > 0) {
                for (int i = 0; i < images.size(); i++) {
                    if (images.get(i).getAsJsonObject().get("width").getAsInt() >= sz) {
                        dto.image = images.get(i).getAsJsonObject().get("url").getAsString();
                        break;
                    }
                }
                if (dto.image == null)
                    dto.image = images.get(images.size() - 1).getAsJsonObject().get("url").getAsString();
            }
        } else if (dto.image == null && hasArray(root, "first_frame")) {
            JsonArray images = root.getAsJsonArray("first_frame");
            if (images.size() > 0) {
                for (int i = 0; i < images.size(); i++) {
                    if (images.get(i).getAsJsonObject().get("width").getAsInt() >= 800) {
                        dto.image = images.get(i).getAsJsonObject().get("url").getAsString();
                        break;
                    }
                }
                if (dto.image == null)
                    dto.image = images.get(images.size() - 1).getAsJsonObject().get("url").getAsString();
            }
        } else if (dto.image == null) {
            if (root.has("photo_800")) {
                dto.image = optString(root, "photo_800");
            } else if (root.has("photo_320")) {
                dto.image = optString(root, "photo_320");
            }
        }

        dto.platform = optString(root, "platform");

        dto.can_edit = optBoolean(root, "can_edit");
        dto.can_add = optBoolean(root, "can_add");
        dto.is_private = optBoolean(root, "is_private");
        return dto;
    }
}
