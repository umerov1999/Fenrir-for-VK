package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.Collections;

import dev.ragnarok.fenrir.api.model.GroupSettingsDto;

public class GroupSettingsAdapter extends AbsAdapter implements JsonDeserializer<GroupSettingsDto> {
    private static final String TAG = GroupSettingsAdapter.class.getSimpleName();

    @Override
    public GroupSettingsDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        GroupSettingsDto dto = new GroupSettingsDto();
        JsonObject root = json.getAsJsonObject();

        dto.title = optString(root, "title");
        dto.description = optString(root, "description");
        dto.address = optString(root, "address");

        if (hasObject(root, "place")) {
            dto.place = context.deserialize(root.get("place"), GroupSettingsDto.Place.class);
        }

        dto.country_id = optInt(root, "country_id");
        dto.city_id = optInt(root, "city_id");
        dto.wall = optInt(root, "wall");
        dto.photos = optInt(root, "photos");
        dto.video = optInt(root, "video");
        dto.audio = optInt(root, "audio");
        dto.docs = optInt(root, "docs");
        dto.topics = optInt(root, "topics");
        dto.wiki = optInt(root, "wiki");
        dto.obscene_filter = optBoolean(root, "obscene_filter");
        dto.obscene_stopwords = optBoolean(root, "obscene_stopwords");
        dto.obscene_words = optStringArray(root, "obscene_words", new String[0]);
        dto.access = optInt(root, "access");
        dto.subject = optInt(root, "subject");
        dto.public_date = optString(root, "public_date");
        dto.public_date_label = optString(root, "public_date_label");

        JsonElement publicCategoryJson = root.get("public_category");
        if (publicCategoryJson instanceof JsonPrimitive) {
            try {
                dto.public_category = String.valueOf(publicCategoryJson.getAsInt());
            } catch (Exception e) {
                dto.public_category = publicCategoryJson.getAsString();
            }
        }

        JsonElement publicSubCategoryJson = root.get("public_subcategory");
        if (publicSubCategoryJson instanceof JsonPrimitive) {
            try {
                dto.public_subcategory = String.valueOf(publicSubCategoryJson.getAsInt());
            } catch (Exception e) {
                dto.public_subcategory = publicSubCategoryJson.getAsString();
            }
        }

        if (hasArray(root, "public_category_list")) {
            dto.public_category_list = parseArray(root.getAsJsonArray("public_category_list"),
                    GroupSettingsDto.PublicCategory.class, context, Collections.emptyList());
        }

        dto.contacts = optInt(root, "contacts");
        dto.links = optInt(root, "links");
        dto.events = optInt(root, "events");
        dto.places = optInt(root, "places");
        dto.rss = optBoolean(root, "rss");
        dto.website = optString(root, "website");
        dto.age_limits = optInt(root, "age_limits");

        if (hasObject(root, "market")) {
            dto.market = context.deserialize(root.get("market"), GroupSettingsDto.Market.class);
        }

        return dto;
    }
}