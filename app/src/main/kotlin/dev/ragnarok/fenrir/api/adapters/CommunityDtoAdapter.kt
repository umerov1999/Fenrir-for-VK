package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.VKApiCommunity.Contact
import dev.ragnarok.fenrir.api.util.VKStringUtils
import java.lang.reflect.Type
import java.util.*
import kotlin.math.abs

class CommunityDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiCommunity> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiCommunity {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val root = json.asJsonObject
        val dto = VKApiCommunity()
        dto.id = optInt(root, "id")
        dto.name = optString(root, "name")
        dto.screen_name = optString(
            root,
            "screen_name",
            String.format(Locale.getDefault(), "club%d", abs(dto.id))
        )
        dto.is_closed = optInt(root, "is_closed")
        dto.is_admin = optBoolean(root, "is_admin")
        dto.admin_level = optInt(root, "admin_level")
        dto.is_member = optBoolean(root, "is_member")
        dto.member_status = optInt(root, "member_status")
        dto.photo_50 = optString(root, "photo_50", VKApiCommunity.PHOTO_50)
        dto.photo_100 = optString(root, "photo_100", VKApiCommunity.PHOTO_100)
        dto.photo_200 = optString(root, "photo_200", null)
        when (optString(root, "type", "group")) {
            VKApiCommunity.TYPE_GROUP -> {
                dto.type = VKApiCommunity.Type.GROUP
            }
            VKApiCommunity.TYPE_PAGE -> {
                dto.type = VKApiCommunity.Type.PAGE
            }
            VKApiCommunity.TYPE_EVENT -> {
                dto.type = VKApiCommunity.Type.EVENT
            }
        }
        if (hasObject(root, VKApiCommunity.CITY)) {
            dto.city = context.deserialize(root[VKApiCommunity.CITY], VKApiCity::class.java)
        }
        if (hasObject(root, VKApiCommunity.COUNTRY)) {
            dto.country =
                context.deserialize(root[VKApiCommunity.COUNTRY], VKApiCountry::class.java)
        }
        if (hasObject(root, VKApiCommunity.BAN_INFO)) {
            val banInfo = root.getAsJsonObject(VKApiCommunity.BAN_INFO)
            dto.blacklisted = true
            dto.ban_end_date = optLong(banInfo, "end_date")
            dto.ban_comment = optString(banInfo, "comment")
        }
        if (hasObject(root, VKApiCommunity.PLACE)) {
            dto.place = context.deserialize(root[VKApiCommunity.PLACE], VKApiPlace::class.java)
        }
        dto.description = optString(root, VKApiCommunity.DESCRIPTION)
        dto.wiki_page = optString(root, VKApiCommunity.WIKI_PAGE)
        dto.members_count = optInt(root, VKApiCommunity.MEMBERS_COUNT)
        if (hasObject(root, VKApiCommunity.COUNTERS)) {
            val counters = root.getAsJsonObject(VKApiCommunity.COUNTERS)
            dto.counters = context.deserialize(counters, VKApiCommunity.Counters::class.java)
        }
        if (hasObject(root, "chats_status")) {
            if (dto.counters == null) {
                dto.counters = VKApiCommunity.Counters()
            }
            dto.counters?.chats = optInt(
                root.getAsJsonObject("chats_status"),
                "count",
                VKApiCommunity.Counters.NO_COUNTER
            )
        }
        dto.start_date = optLong(root, VKApiCommunity.START_DATE)
        dto.finish_date = optLong(root, VKApiCommunity.FINISH_DATE)
        dto.can_post = optBoolean(root, VKApiCommunity.CAN_POST)
        dto.can_see_all_posts = optBoolean(root, VKApiCommunity.CAN_SEE_ALL_POSTS)
        dto.can_upload_doc = optBoolean(root, VKApiCommunity.CAN_UPLOAD_DOC)
        dto.can_upload_video = optBoolean(root, VKApiCommunity.CAN_UPLOAD_VIDEO)
        dto.can_create_topic = optBoolean(root, VKApiCommunity.CAN_CTARE_TOPIC)
        dto.is_favorite = optBoolean(root, VKApiCommunity.IS_FAVORITE)
        dto.is_subscribed = optBoolean(root, VKApiCommunity.IS_SUBSCRIBED)
        dto.status = VKStringUtils.unescape(optString(root, VKApiCommunity.STATUS))
        if (hasObject(root, "status_audio")) {
            dto.status_audio = context.deserialize(root["status_audio"], VKApiAudio::class.java)
        }
        dto.contacts = parseArray(
            root.getAsJsonArray(VKApiCommunity.CONTACTS),
            Contact::class.java,
            context,
            null
        )
        dto.links = parseArray(
            root.getAsJsonArray(VKApiCommunity.LINKS),
            VKApiCommunity.Link::class.java,
            context,
            null
        )
        dto.fixed_post = optInt(root, VKApiCommunity.FIXED_POST)
        dto.main_album_id = optInt(root, VKApiCommunity.MAIN_ALBUM_ID)
        dto.verified = optBoolean(root, VKApiCommunity.VERIFIED)
        dto.site = optString(root, VKApiCommunity.SITE)
        dto.activity = optString(root, VKApiCommunity.ACTIVITY)
        dto.can_message = optBoolean(root, "can_message")
        if (hasObject(root, "cover")) {
            dto.cover = context.deserialize(root["cover"], VKApiCover::class.java)
        }
        return dto
    }

    companion object {
        private val TAG = CommunityDtoAdapter::class.java.simpleName
    }
}