package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiArticle;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;

public class ArticleDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiArticle> {

    private static final String TAG = ArticleDtoAdapter.class.getSimpleName();

    @Override
    public VKApiArticle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiArticle article = new VKApiArticle();
        JsonObject root = json.getAsJsonObject();

        article.id = optInt(root, "id");
        article.owner_id = optInt(root, "owner_id");
        article.owner_name = optString(root, "owner_name");
        article.title = optString(root, "title");
        article.subtitle = optString(root, "subtitle");
        article.access_key = optString(root, "access_key");
        article.is_favorite = optBoolean(root, "is_favorite");
        if (hasObject(root, "photo")) {
            article.photo = context.deserialize(root.get("photo"), VKApiPhoto.class);
        }
        if (root.has("view_url")) {
            article.url = optString(root, "view_url");
        } else if (root.has("url")) {
            article.url = optString(root, "url");
        }

        return article;
    }
}
