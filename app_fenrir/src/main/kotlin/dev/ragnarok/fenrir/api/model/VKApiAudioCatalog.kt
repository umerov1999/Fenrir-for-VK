package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VKApiAudioCatalog {
    @SerialName("id")
    var id: String? = null

    @SerialName("source")
    var source: String? = null

    @SerialName("next_from")
    var next_from: String? = null

    @SerialName("subtitle")
    var subtitle: String? = null

    @SerialName("title")
    var title: String? = null

    @SerialName("type")
    var type: String? = null

    @SerialName("count")
    var count = 0

    @SerialName("audios")
    var audios: List<VKApiAudio>? = null

    @SerialName("playlists")
    var playlists: List<VKApiAudioPlaylist>? = null

    @SerialName("playlist")
    var playlist: VKApiAudioPlaylist? = null

    @SerialName("videos")
    var videos: List<VKApiVideo>? = null

    @SerialName("items")
    var items: List<VKApiCatalogLink>? = null

    @SerialName("artist")
    var artist: VKApiArtistBlock? = null

    @Serializable
    class VKApiArtistBlock {
        @SerialName("name")
        var name: String? = null

        @SerialName("photo")
        var images: List<Image>? = null
    }

    @Serializable
    class Image {
        @SerialName("url")
        var url: String? = null

        @SerialName("width")
        var width = 0

        @SerialName("height")
        var height = 0
    }
}