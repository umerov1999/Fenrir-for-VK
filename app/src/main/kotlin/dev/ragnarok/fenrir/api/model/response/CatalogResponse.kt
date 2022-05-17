package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiAudio
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist
import dev.ragnarok.fenrir.api.model.VKApiCatalogLink
import dev.ragnarok.fenrir.api.model.VKApiVideo

class CatalogResponse {
    @SerializedName("audios")
    var audios: List<VKApiAudio>? = null

    @SerializedName("playlists")
    var playlists: List<VKApiAudioPlaylist>? = null

    @SerializedName("videos")
    var videos: List<VKApiVideo>? = null

    @SerializedName("items")
    var items: List<VKApiCatalogLink>? = null

    @SerializedName("next_from")
    var nextFrom: String? = null
}