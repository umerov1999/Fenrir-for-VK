package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiMarket
import java.lang.reflect.Type

class MarketDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiMarket> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiMarket {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = VKApiMarket()
        val root = json.asJsonObject
        dto.id = optInt(root, "id")
        dto.owner_id = optInt(root, "owner_id")
        dto.weight = optInt(root, "weight")
        dto.availability = optInt(root, "availability")
        dto.date = optInt(root, "date")
        dto.title = optString(root, "title")
        dto.description = optString(root, "description")
        dto.thumb_photo = optString(root, "thumb_photo")
        dto.sku = optString(root, "sku")
        dto.access_key = optString(root, "access_key")
        dto.is_favorite = optBoolean(root, "is_favorite")
        if (hasObject(root, "dimensions")) {
            val dimensions = root["dimensions"].asJsonObject
            dto.dimensions = optInt(dimensions, "length").toString() + "x" + optInt(
                dimensions,
                "width"
            ) + "x" + optInt(dimensions, "height") + " mm"
        }
        if (hasObject(root, "price")) {
            val price = root["price"].asJsonObject
            dto.price = optString(price, "text")
        }
        return dto
    }

    companion object {
        private val TAG = MarketDtoAdapter::class.java.simpleName
    }
}