package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Assets<I> {
    @SerialName("version")
    var version = 0

    @SerialName("assets")
    var assets: ArrayList<I>? = null

    /**
     * Reactions that can be sent, in display order. Assets not listed here
     * are only kept for rendering reactions that already exist on messages.
     */
    @SerialName("reaction_ids")
    var reaction_ids: List<Int>? = null
}
