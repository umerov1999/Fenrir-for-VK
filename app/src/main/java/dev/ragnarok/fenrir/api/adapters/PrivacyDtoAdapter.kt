package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VkApiPrivacy
import java.lang.reflect.Type

class PrivacyDtoAdapter : AbsAdapter(), JsonDeserializer<VkApiPrivacy> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VkApiPrivacy {
        if (!checkObject(json)) {
            return VkApiPrivacy("null")
        }
        val root = json.asJsonObject

        // Examples
        // {"category":"only_me"}
        // {"owners":{"allowed":[13326918,26632922,31182820,50949233,113672278,138335672]}}
        val privacy = VkApiPrivacy(optString(root, "category", "only_me"))
        val owners = root["owners"]
        if (checkObject(owners)) {
            val allowed = owners.asJsonObject["allowed"]
            if (checkArray(allowed)) {
                for (i in 0 until allowed.asJsonArray.size()) {
                    privacy.includeOwner(optInt(allowed.asJsonArray, i))
                }
            }
            val excluded = owners.asJsonObject["excluded"]
            if (checkArray(excluded)) {
                for (i in 0 until excluded.asJsonArray.size()) {
                    privacy.excludeOwner(optInt(excluded.asJsonArray, i))
                }
            }
        }
        val lists = root["lists"]
        if (checkObject(lists)) {
            val allowed = lists.asJsonObject["allowed"]
            if (checkArray(allowed)) {
                for (i in 0 until allowed.asJsonArray.size()) {
                    privacy.includeFriendsList(optInt(allowed.asJsonArray, i))
                }
            }
            val excluded = lists.asJsonObject["excluded"]
            if (checkArray(excluded)) {
                for (i in 0 until excluded.asJsonArray.size()) {
                    privacy.excludeFriendsList(optInt(excluded.asJsonArray, i))
                }
            }
        }
        return privacy
    }
}