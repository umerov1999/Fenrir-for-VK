package dev.ragnarok.fenrir.db.serialize

import com.google.gson.Gson
import dev.ragnarok.fenrir.model.Photo

object Serializers {
    private val GSON = Gson()

    @JvmField
    val PHOTOS_SERIALIZER: ISerializeAdapter<Photo> = object : ISerializeAdapter<Photo> {
        override fun deserialize(raw: String): Photo {
            return GSON.fromJson(raw, Photo::class.java)
        }

        override fun serialize(data: Photo): String {
            return GSON.toJson(data)
        }
    }
}