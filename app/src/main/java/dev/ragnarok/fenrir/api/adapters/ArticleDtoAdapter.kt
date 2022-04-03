package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiArticle
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import java.lang.reflect.Type

class ArticleDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiArticle> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiArticle {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val article = VKApiArticle()
        val root = json.asJsonObject
        article.id = optInt(root, "id")
        article.owner_id = optInt(root, "owner_id")
        article.owner_name = optString(root, "owner_name")
        article.title = optString(root, "title")
        article.subtitle = optString(root, "subtitle")
        article.access_key = optString(root, "access_key")
        article.is_favorite = optBoolean(root, "is_favorite")
        if (hasObject(root, "photo")) {
            article.photo = context.deserialize(root["photo"], VKApiPhoto::class.java)
        }
        if (root.has("view_url")) {
            article.url = optString(root, "view_url")
        } else if (root.has("url")) {
            article.url = optString(root, "url")
        }
        return article
    }

    companion object {
        private val TAG = ArticleDtoAdapter::class.java.simpleName
    }
}