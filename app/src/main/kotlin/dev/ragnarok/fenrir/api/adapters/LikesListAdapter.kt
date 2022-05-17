package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiOwner
import dev.ragnarok.fenrir.api.model.VKApiUser
import dev.ragnarok.fenrir.api.model.response.LikesListResponse
import dev.ragnarok.fenrir.requireNonNull
import java.lang.reflect.Type

class LikesListAdapter : AbsAdapter(), JsonDeserializer<LikesListResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LikesListResponse {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val response = LikesListResponse()
        val root = json.asJsonObject
        response.count = optInt(root, "count")
        if (hasArray(root, "items")) {
            val itemsArray = root.getAsJsonArray("items")
            response.owners = ArrayList(itemsArray.size())
            for (i in 0 until itemsArray.size()) {
                if (!checkObject(itemsArray[i])) {
                    continue
                }
                val itemRoot = itemsArray[i].asJsonObject
                val type = optString(itemRoot, "type")
                var owner: VKApiOwner? = null
                if ("profile" == type) {
                    owner = context.deserialize(itemRoot, VKApiUser::class.java)
                } else if ("group" == type || "page" == type) {
                    owner = context.deserialize(itemRoot, VKApiCommunity::class.java)
                }
                owner.requireNonNull {
                    response.owners?.add(it)
                }
            }
        }
        return response
    }

    companion object {
        private val TAG = LikesListAdapter::class.java.simpleName
    }
}