package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity

class GroupWallInfoResponse {
    @SerializedName("group_info")
    var groups: List<VKApiCommunity>? = null

    @SerializedName("all_wall_count")
    var allWallCount: Int? = null

    @SerializedName("owner_wall_count")
    var ownerWallCount: Int? = null

    @SerializedName("suggests_wall_count")
    var suggestsWallCount: Int? = null

    @SerializedName("postponed_wall_count")
    var postponedWallCount: Int? = null
}