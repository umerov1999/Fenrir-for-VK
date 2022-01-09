package dev.ragnarok.fenrir.api.adapters;

import static dev.ragnarok.fenrir.api.model.VKApiUser.CAMERA_50;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.ABOUT;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.ACTIVITIES;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.ACTIVITY;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.BDATE;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.BLACKLISTED_BY_ME;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.BOOKS;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.CAN_POST;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.CAN_SEE_ALL_POSTS;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.CAN_WRITE_PRIVATE_MESSAGE;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.CAREER;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.CITY;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.COUNTERS;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.COUNTRY;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.GAMES;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.HOME_TOWN;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.INTERESTS;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.LAST_SEEN;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.MILITARY;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.MOVIES;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.ONLINE;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.ONLINE_MOBILE;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.PERSONAL;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.PHOTO_100;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.PHOTO_200;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.PHOTO_50;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.PHOTO_MAX_ORIG;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.QUOTES;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.RELATION;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.RELATIVES;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.SCHOOLS;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.SEX;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.SITE;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.STATUS;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.TV;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.UNIVERSITIES;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.VERIFIED;
import static dev.ragnarok.fenrir.api.model.VKApiUser.Field.WALL_DEFAULT;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Collections;

import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiCareer;
import dev.ragnarok.fenrir.api.model.VKApiCity;
import dev.ragnarok.fenrir.api.model.VKApiCountry;
import dev.ragnarok.fenrir.api.model.VKApiMilitary;
import dev.ragnarok.fenrir.api.model.VKApiSchool;
import dev.ragnarok.fenrir.api.model.VKApiUniversity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.util.VKStringUtils;

public class UserDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiUser> {
    private static final String TAG = UserDtoAdapter.class.getSimpleName();

    @Override
    public VKApiUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        VKApiUser dto = new VKApiUser();
        JsonObject root = json.getAsJsonObject();

        dto.id = optInt(root, "id");
        if (dto.id == 0)
            dto.id = optInt(root, "user_id");
        dto.first_name = optString(root, "first_name");
        dto.last_name = optString(root, "last_name");
        dto.online = optBoolean(root, ONLINE);
        dto.online_mobile = optBoolean(root, ONLINE_MOBILE);
        dto.online_app = optInt(root, "online_app");

        dto.photo_50 = optString(root, PHOTO_50, CAMERA_50);
        dto.photo_100 = optString(root, PHOTO_100);
        dto.photo_200 = optString(root, PHOTO_200);

        if (hasObject(root, LAST_SEEN)) {
            JsonObject lastSeenRoot = root.getAsJsonObject(LAST_SEEN);
            dto.last_seen = optLong(lastSeenRoot, "time");
            dto.platform = optInt(lastSeenRoot, "platform");
        }

        dto.photo_max_orig = optString(root, PHOTO_MAX_ORIG);
        dto.status = VKStringUtils.unescape(optString(root, STATUS));

        dto.bdate = optString(root, BDATE);

        if (hasObject(root, CITY)) {
            dto.city = context.deserialize(root.get(CITY), VKApiCity.class);
        }

        if (hasObject(root, COUNTRY)) {
            dto.country = context.deserialize(root.get(COUNTRY), VKApiCountry.class);
        }

        dto.universities = parseArray(root.get(UNIVERSITIES), VKApiUniversity.class, context, null);
        dto.schools = parseArray(root.get(SCHOOLS), VKApiSchool.class, context, null);
        dto.militaries = parseArray(root.get(MILITARY), VKApiMilitary.class, context, null);
        dto.careers = parseArray(root.get(CAREER), VKApiCareer.class, context, null);

        // status
        dto.activity = optString(root, ACTIVITY);

        if (hasObject(root, "status_audio")) {
            dto.status_audio = context.deserialize(root.get("status_audio"), VKApiAudio.class);
        }

        if (hasObject(root, PERSONAL)) {
            JsonObject personal = root.getAsJsonObject(PERSONAL);
            dto.smoking = optInt(personal, "smoking");
            dto.alcohol = optInt(personal, "alcohol");
            dto.political = optInt(personal, "political");
            dto.life_main = optInt(personal, "life_main");
            dto.people_main = optInt(personal, "people_main");
            dto.inspired_by = optString(personal, "inspired_by");
            dto.religion = optString(personal, "religion");

            if (hasArray(personal, "langs")) {
                JsonArray langs = personal.get("langs").getAsJsonArray();
                dto.langs = new String[langs.size()];
                for (int i = 0; i < langs.size(); i++) {
                    dto.langs[i] = optString(langs, i);
                }
            }
        }

        // contacts
        dto.facebook = optString(root, "facebook");
        dto.facebook_name = optString(root, "facebook_name");
        dto.livejournal = optString(root, "livejournal");
        dto.site = optString(root, SITE);
        dto.screen_name = optString(root, "screen_name", "id" + dto.id);
        dto.skype = optString(root, "skype");
        dto.mobile_phone = optString(root, "mobile_phone");
        dto.home_phone = optString(root, "home_phone");
        dto.twitter = optString(root, "twitter");
        dto.instagram = optString(root, "instagram");

        // personal info
        dto.about = optString(root, ABOUT);
        dto.activities = optString(root, ACTIVITIES);
        dto.books = optString(root, BOOKS);
        dto.games = optString(root, GAMES);
        dto.interests = optString(root, INTERESTS);
        dto.movies = optString(root, MOVIES);
        dto.quotes = optString(root, QUOTES);
        dto.tv = optString(root, TV);

        // settings
        dto.nickname = optString(root, "nickname");
        dto.domain = optString(root, "domain");
        dto.can_post = optBoolean(root, CAN_POST);
        dto.can_see_all_posts = optBoolean(root, CAN_SEE_ALL_POSTS);
        dto.blacklisted_by_me = optBoolean(root, BLACKLISTED_BY_ME);
        dto.can_write_private_message = optBoolean(root, CAN_WRITE_PRIVATE_MESSAGE);
        dto.wall_comments = optBoolean(root, WALL_DEFAULT);

        String deactivated = optString(root, "deactivated");
        dto.is_deleted = "deleted".equals(deactivated);
        dto.is_banned = "banned".equals(deactivated);

        dto.wall_default_owner = "owner".equals(optString(root, WALL_DEFAULT));
        dto.verified = optBoolean(root, VERIFIED);

        dto.can_access_closed = optBoolean(root, "can_access_closed");

        // other
        dto.sex = optInt(root, SEX);

        if (hasObject(root, COUNTERS)) {
            dto.counters = context.deserialize(root.get(COUNTERS), VKApiUser.Counters.class);
        }

        dto.relation = optInt(root, RELATION);
        dto.relatives = parseArray(root.get(RELATIVES), VKApiUser.Relative.class,
                context, Collections.emptyList());

        dto.home_town = optString(root, HOME_TOWN);

        dto.photo_id = optString(root, "photo_id");
        dto.blacklisted = optBoolean(root, "blacklisted");
        dto.photo_200_orig = optString(root, "photo_200_orig");
        dto.photo_400_orig = optString(root, "photo_400_orig");
        dto.photo_max = optString(root, "photo_max");
        dto.has_mobile = optBoolean(root, "has_mobile");

        if (hasObject(root, "occupation")) {
            dto.occupation = context.deserialize(root.get("occupation"), VKApiUser.Occupation.class);
        }

        if (hasObject(root, "relation_partner")) {
            dto.relation_partner = deserialize(root.get("relation_partner"), VKApiUser.class, context);
        }

        dto.music = optString(root, "music");
        dto.can_see_audio = optBoolean(root, "can_see_audio");
        dto.can_send_friend_request = optBoolean(root, "can_send_friend_request");
        dto.is_favorite = optBoolean(root, "is_favorite");
        dto.is_subscribed = optBoolean(root, "is_subscribed");
        dto.timezone = optInt(root, "timezone");
        dto.maiden_name = optString(root, "maiden_name");
        dto.is_friend = optBoolean(root, "is_friend");
        dto.friend_status = optInt(root, "friend_status");
        dto.role = optString(root, "role");
        return dto;
    }
}
