package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiConversationMembers {
    @SerializedName("member_id")
    var member_id = 0

    @SerializedName("invited_by")
    var invited_by = 0

    @SerializedName("join_date")
    var join_date: Long = 0

    @SerializedName("is_admin")
    var is_admin = false

    @SerializedName("is_owner")
    var is_owner = false

    @SerializedName("can_kick")
    var can_kick = false
}