package dev.ragnarok.fenrir.api.model

import dev.ragnarok.fenrir.api.adapters.ReactionAssetDtoAdapter
import kotlinx.serialization.Serializable

@Serializable(with = ReactionAssetDtoAdapter::class)
class VKApiReactionAsset {
    var big_animation: String? = null
    var small_animation: String? = null
    var static: String? = null
    var reaction_id = 0

    /**
     * Position in the reaction picker, or -1 if VK does not allow sending it.
     * Filled from `reaction_ids`, not parsed from the asset object.
     */
    var position = -1
}
