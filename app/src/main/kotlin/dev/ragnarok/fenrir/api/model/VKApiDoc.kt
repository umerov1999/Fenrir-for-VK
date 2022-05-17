package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiDoc : VKApiAttachment {
    @SerializedName("id")
    var id = 0

    @SerializedName("owner_id")
    var ownerId = 0

    @SerializedName("title")
    var title: String? = null

    @SerializedName("size")
    var size: Long = 0

    @SerializedName("ext")
    var ext: String? = null

    @SerializedName("url")
    var url: String? = null

    @SerializedName("date")
    var date: Long = 0

    @SerializedName("type")
    var type = 0

    @SerializedName("preview")
    var preview: Preview? = null

    @SerializedName("access_key")
    var accessKey: String? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_DOC
    }

    class Entry(val type: String, val doc: VKApiDoc)
    class Preview {
        @SerializedName("photo")
        var photo: Photo? = null

        @SerializedName("video")
        var video: Video? = null

        @SerializedName("graffiti")
        var graffiti: Graffiti? = null
    }

    class Graffiti {
        @SerializedName("src")
        var src: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0
    }

    class Photo {
        @SerializedName("sizes")
        var sizes: List<PhotoSizeDto>? = null
    }

    class Video {
        @SerializedName("src")
        var src: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0

        @SerializedName("file_size")
        var fileSize: Long = 0
    }
}