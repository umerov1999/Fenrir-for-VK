package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiUser

class UserWallInfoResponse {
    @SerializedName("user_info")
    var users: List<VKApiUser>? = null

    @SerializedName("all_wall_count")
    var allWallCount: Int? = null

    @SerializedName("owner_wall_count")
    var ownerWallCount: Int? = null

    @SerializedName("postponed_wall_count")
    var postponedWallCount: Int? = null
}