package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiAudioCatalog {
    @SerializedName("id")
    var id: String? = null

    @SerializedName("source")
    var source: String? = null

    @SerializedName("next_from")
    var next_from: String? = null

    @SerializedName("subtitle")
    var subtitle: String? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("count")
    var count = 0

    @SerializedName("audios")
    var audios: List<VKApiAudio>? = null

    @SerializedName("playlists")
    var playlists: List<VKApiAudioPlaylist>? = null

    @SerializedName("playlist")
    var playlist: VKApiAudioPlaylist? = null

    @SerializedName("videos")
    var videos: List<VKApiVideo>? = null

    @SerializedName("items")
    var items: List<VKApiCatalogLink>? = null

    @SerializedName("artist")
    var artist: VKApiArtistBlock? = null

    class VKApiArtistBlock {
        @SerializedName("name")
        var name: String? = null

        @SerializedName("photo")
        var images: List<Image>? = null
    }

    class Image {
        @SerializedName("url")
        var url: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0
    }
}