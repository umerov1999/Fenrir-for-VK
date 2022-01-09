package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VkApiPrivacy;

public class PrivacyDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiPrivacy> {

    @Override
    public VkApiPrivacy deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            return new VkApiPrivacy("null");
        }
        JsonObject root = json.getAsJsonObject();

        // Examples
        // {"category":"only_me"}
        // {"owners":{"allowed":[13326918,26632922,31182820,50949233,113672278,138335672]}}
        VkApiPrivacy privacy = new VkApiPrivacy(optString(root, "category", "only_me"));

        JsonElement owners = root.get("owners");

        if (checkObject(owners)) {
            JsonElement allowed = owners.getAsJsonObject().get("allowed");
            if (checkArray(allowed)) {
                for (int i = 0; i < allowed.getAsJsonArray().size(); i++) {
                    privacy.includeOwner(optInt(allowed.getAsJsonArray(), i));
                }
            }

            JsonElement excluded = owners.getAsJsonObject().get("excluded");
            if (checkArray(excluded)) {
                for (int i = 0; i < excluded.getAsJsonArray().size(); i++) {
                    privacy.excludeOwner(optInt(excluded.getAsJsonArray(), i));
                }
            }
        }

        JsonElement lists = root.get("lists");
        if (checkObject(lists)) {
            JsonElement allowed = lists.getAsJsonObject().get("allowed");
            if (checkArray(allowed)) {
                for (int i = 0; i < allowed.getAsJsonArray().size(); i++) {
                    privacy.includeFriendsList(optInt(allowed.getAsJsonArray(), i));
                }
            }

            JsonElement excluded = lists.getAsJsonObject().get("excluded");
            if (checkArray(excluded)) {
                for (int i = 0; i < excluded.getAsJsonArray().size(); i++) {
                    privacy.excludeFriendsList(optInt(excluded.getAsJsonArray(), i));
                }
            }
        }

        return privacy;
    }
}