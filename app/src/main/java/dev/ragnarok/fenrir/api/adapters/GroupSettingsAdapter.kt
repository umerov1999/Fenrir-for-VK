package dev.ragnarok.fenrir.api.adapters

import com.google.gson.*
import dev.ragnarok.fenrir.api.model.GroupSettingsDto
import dev.ragnarok.fenrir.api.model.GroupSettingsDto.PublicCategory
import java.lang.reflect.Type

class GroupSettingsAdapter : AbsAdapter(), JsonDeserializer<GroupSettingsDto> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): GroupSettingsDto {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dto = GroupSettingsDto()
        val root = json.asJsonObject
        dto.title = optString(root, "title")
        dto.description = optString(root, "description")
        dto.address = optString(root, "address")
        if (hasObject(root, "place")) {
            dto.place = context.deserialize(root["place"], GroupSettingsDto.Place::class.java)
        }
        dto.country_id = optInt(root, "country_id")
        dto.city_id = optInt(root, "city_id")
        dto.wall = optInt(root, "wall")
        dto.photos = optInt(root, "photos")
        dto.video = optInt(root, "video")
        dto.audio = optInt(root, "audio")
        dto.docs = optInt(root, "docs")
        dto.topics = optInt(root, "topics")
        dto.wiki = optInt(root, "wiki")
        dto.obscene_filter = optBoolean(root, "obscene_filter")
        dto.obscene_stopwords = optBoolean(root, "obscene_stopwords")
        dto.obscene_words = optStringArray(root, "obscene_words", arrayOfNulls(0))
        dto.access = optInt(root, "access")
        dto.subject = optInt(root, "subject")
        dto.public_date = optString(root, "public_date")
        dto.public_date_label = optString(root, "public_date_label")
        val publicCategoryJson = root["public_category"]
        if (publicCategoryJson is JsonPrimitive) {
            try {
                dto.public_category = publicCategoryJson.getAsInt().toString()
            } catch (e: Exception) {
                dto.public_category = publicCategoryJson.getAsString()
            }
        }
        val publicSubCategoryJson = root["public_subcategory"]
        if (publicSubCategoryJson is JsonPrimitive) {
            try {
                dto.public_subcategory = publicSubCategoryJson.getAsInt().toString()
            } catch (e: Exception) {
                dto.public_subcategory = publicSubCategoryJson.getAsString()
            }
        }
        if (hasArray(root, "public_category_list")) {
            dto.public_category_list = parseArray(
                root.getAsJsonArray("public_category_list"),
                PublicCategory::class.java, context, emptyList()
            )
        }
        dto.contacts = optInt(root, "contacts")
        dto.links = optInt(root, "links")
        dto.events = optInt(root, "events")
        dto.places = optInt(root, "places")
        dto.rss = optBoolean(root, "rss")
        dto.website = optString(root, "website")
        dto.age_limits = optInt(root, "age_limits")
        if (hasObject(root, "market")) {
            dto.market = context.deserialize(root["market"], GroupSettingsDto.Market::class.java)
        }
        return dto
    }

    companion object {
        private val TAG = GroupSettingsAdapter::class.java.simpleName
    }
}