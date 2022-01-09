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
import dev.ragnarok.fenrir.api.model.PhotoSizeDto;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;

public class PhotoDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiPhoto> {
    private static final String TAG = PhotoDtoAdapter.class.getSimpleName();

    @Override
    public VKApiPhoto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiPhoto photo = new VKApiPhoto();
        JsonObject root = json.getAsJsonObject();

        photo.id = optInt(root, "id");
        photo.album_id = optInt(root, "album_id");
        photo.date = optLong(root, "date");
        photo.height = optInt(root, "height");
        photo.width = optInt(root, "width");
        photo.owner_id = optInt(root, "owner_id");
        photo.text = optString(root, "text");
        photo.access_key = optString(root, "access_key");

        if (hasObject(root, "likes")) {
            JsonObject likesRoot = root.get("likes").getAsJsonObject();
            photo.likes = optInt(likesRoot, "count");
            photo.user_likes = optBoolean(likesRoot, "user_likes");
        }

        if (hasObject(root, "comments")) {
            photo.comments = context.deserialize(root.get("comments"), CommentsDto.class);
        }

        if (hasObject(root, "tags")) {
            JsonObject tagsRoot = root.get("tags").getAsJsonObject();
            photo.tags = optInt(tagsRoot, "count");
        }

        photo.can_comment = optBoolean(root, "can_comment");
        photo.post_id = optInt(root, "post_id");

        if (hasArray(root, "sizes")) {
            JsonArray sizesArray = root.getAsJsonArray("sizes");
            photo.sizes = new ArrayList<>(sizesArray.size());

            for (int i = 0; i < sizesArray.size(); i++) {
                if (!checkObject(sizesArray.get(i))) {
                    continue;
                }
                PhotoSizeDto photoSizeDto = context.deserialize(sizesArray.get(i).getAsJsonObject(), PhotoSizeDto.class);
                photo.sizes.add(photoSizeDto);

                //find biggest photo size
                switch (photoSizeDto.type) {
                    case PhotoSizeDto.Type.O:
                    case PhotoSizeDto.Type.P:
                    case PhotoSizeDto.Type.Q:
                    case PhotoSizeDto.Type.R:
                        continue;

                    default:
                        if (photo.width > photoSizeDto.width && photo.height > photoSizeDto.height) {
                            continue;
                        }

                        photo.width = photoSizeDto.width;
                        photo.height = photoSizeDto.height;
                        break;
                }
            }
        }

        return photo;
    }
}