package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiStory;
import dev.ragnarok.fenrir.api.model.VKApiVideo;

public class StoryDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiStory> {
    private static final String TAG = StoryDtoAdapter.class.getSimpleName();

    @Override
    public VKApiStory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiStory story = new VKApiStory();
        JsonObject root = json.getAsJsonObject();

        story.id = optInt(root, "id");
        story.owner_id = optInt(root, "owner_id");
        story.date = optInt(root, "owner_id");
        story.expires_at = optInt(root, "expires_at");
        story.is_expired = optBoolean(root, "is_expired");
        story.is_ads = optBoolean(root, "is_ads");
        if (hasObject(root, "photo")) {
            story.photo = context.deserialize(root.get("photo"), VKApiPhoto.class);
        }
        if (hasObject(root, "video")) {
            story.video = context.deserialize(root.get("video"), VKApiVideo.class);
        }
        if (hasObject(root, "parent_story")) {
            story.parent_story = context.deserialize(root.get("parent_story"), VKApiStory.class);
        }
        if (hasObject(root, "link")) {
            story.target_url = optString(root.getAsJsonObject("link"), "url");
        }
        return story;
    }
}
