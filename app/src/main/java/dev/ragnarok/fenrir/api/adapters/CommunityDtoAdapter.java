package dev.ragnarok.fenrir.api.adapters;

import static dev.ragnarok.fenrir.api.model.VKApiCommunity.ACTIVITY;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.BAN_INFO;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.CAN_CTARE_TOPIC;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.CAN_POST;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.CAN_SEE_ALL_POSTS;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.CAN_UPLOAD_DOC;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.CAN_UPLOAD_VIDEO;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.CITY;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.CONTACTS;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.COUNTERS;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.COUNTRY;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.DESCRIPTION;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.FINISH_DATE;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.FIXED_POST;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.IS_FAVORITE;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.IS_SUBSCRIBED;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.LINKS;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.MAIN_ALBUM_ID;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.MEMBERS_COUNT;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.PHOTO_100;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.PHOTO_50;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.PLACE;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.SITE;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.START_DATE;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.STATUS;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.TYPE_EVENT;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.TYPE_GROUP;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.TYPE_PAGE;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.VERIFIED;
import static dev.ragnarok.fenrir.api.model.VKApiCommunity.WIKI_PAGE;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Objects;

import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiCity;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiCountry;
import dev.ragnarok.fenrir.api.model.VKApiPlace;
import dev.ragnarok.fenrir.api.model.VkApiCover;
import dev.ragnarok.fenrir.api.util.VKStringUtils;

public class CommunityDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiCommunity> {
    private static final String TAG = CommunityDtoAdapter.class.getSimpleName();

    @Override
    public VKApiCommunity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        JsonObject root = json.getAsJsonObject();
        VKApiCommunity dto = new VKApiCommunity();

        dto.id = optInt(root, "id");
        dto.name = optString(root, "name");
        dto.screen_name = optString(root, "screen_name", String.format(Locale.getDefault(), "club%d", Math.abs(dto.id)));
        dto.is_closed = optInt(root, "is_closed");

        dto.is_admin = optBoolean(root, "is_admin");
        dto.admin_level = optInt(root, "admin_level");

        dto.is_member = optBoolean(root, "is_member");
        dto.member_status = optInt(root, "member_status");

        dto.photo_50 = optString(root, "photo_50", PHOTO_50);
        dto.photo_100 = optString(root, "photo_100", PHOTO_100);
        dto.photo_200 = optString(root, "photo_200", null);

        String type = optString(root, "type", "group");
        if (TYPE_GROUP.equals(type)) {
            dto.type = VKApiCommunity.Type.GROUP;
        } else if (TYPE_PAGE.equals(type)) {
            dto.type = VKApiCommunity.Type.PAGE;
        } else if (TYPE_EVENT.equals(type)) {
            dto.type = VKApiCommunity.Type.EVENT;
        }

        if (hasObject(root, CITY)) {
            dto.city = context.deserialize(root.get(CITY), VKApiCity.class);
        }

        if (hasObject(root, COUNTRY)) {
            dto.country = context.deserialize(root.get(COUNTRY), VKApiCountry.class);
        }

        if (hasObject(root, BAN_INFO)) {
            JsonObject banInfo = root.getAsJsonObject(BAN_INFO);
            dto.blacklisted = true;
            dto.ban_end_date = optLong(banInfo, "end_date");
            dto.ban_comment = optString(banInfo, "comment");
        }

        if (hasObject(root, PLACE)) {
            dto.place = context.deserialize(root.get(PLACE), VKApiPlace.class);
        }

        dto.description = optString(root, DESCRIPTION);
        dto.wiki_page = optString(root, WIKI_PAGE);
        dto.members_count = optInt(root, MEMBERS_COUNT);

        if (hasObject(root, COUNTERS)) {
            JsonObject counters = root.getAsJsonObject(COUNTERS);
            dto.counters = context.deserialize(counters, VKApiCommunity.Counters.class);
        }
        if (hasObject(root, "chats_status")) {
            if (Objects.isNull(dto.counters)) {
                dto.counters = new VKApiCommunity.Counters();
            }
            dto.counters.chats = optInt(root.getAsJsonObject("chats_status"), "count", VKApiCommunity.Counters.NO_COUNTER);
        }

        dto.start_date = optLong(root, START_DATE);
        dto.finish_date = optLong(root, FINISH_DATE);
        dto.can_post = optBoolean(root, CAN_POST);
        dto.can_see_all_posts = optBoolean(root, CAN_SEE_ALL_POSTS);
        dto.can_upload_doc = optBoolean(root, CAN_UPLOAD_DOC);
        dto.can_upload_video = optBoolean(root, CAN_UPLOAD_VIDEO);
        dto.can_create_topic = optBoolean(root, CAN_CTARE_TOPIC);
        dto.is_favorite = optBoolean(root, IS_FAVORITE);
        dto.is_subscribed = optBoolean(root, IS_SUBSCRIBED);
        dto.status = VKStringUtils.unescape(optString(root, STATUS));

        if (hasObject(root, "status_audio")) {
            dto.status_audio = context.deserialize(root.get("status_audio"), VKApiAudio.class);
        }

        dto.contacts = parseArray(root.getAsJsonArray(CONTACTS), VKApiCommunity.Contact.class, context, null);
        dto.links = parseArray(root.getAsJsonArray(LINKS), VKApiCommunity.Link.class, context, null);

        dto.fixed_post = optInt(root, FIXED_POST);
        dto.main_album_id = optInt(root, MAIN_ALBUM_ID);
        dto.verified = optBoolean(root, VERIFIED);
        dto.site = optString(root, SITE);
        dto.activity = optString(root, ACTIVITY);
        dto.can_message = optBoolean(root, "can_message");

        if (hasObject(root, "cover")) {
            dto.cover = context.deserialize(root.get("cover"), VkApiCover.class);
        }

        return dto;
    }
}