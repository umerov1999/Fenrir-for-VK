package dev.ragnarok.fenrir.api.model.response

import dev.ragnarok.fenrir.api.model.VKApiAudio
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist
import dev.ragnarok.fenrir.api.model.VKApiCatalogLink
import dev.ragnarok.fenrir.api.model.VKApiVideo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CatalogResponse {
    @SerialName("audios")
    var audios: List<VKApiAudio>? = null

    @SerialName("playlists")
    var playlists: List<VKApiAudioPlaylist>? = null

    @SerialName("videos")
    var videos: List<VKApiVideo>? = null

    @SerialName("items")
    var items: List<VKApiCatalogLink>? = null

    @SerialName("next_from")
    var nextFrom: String? = null
}