package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiGroupChats {
    @SerializedName("id")
    var id = 0

    @SerializedName("members_count")
    var members_count = 0

    @SerializedName("last_message_date")
    var last_message_date: Long = 0

    @SerializedName("is_closed")
    var is_closed = false

    @SerializedName("invite_link")
    var invite_link: String? = null

    @SerializedName("photo")
    var photo: String? = null

    @SerializedName("title")
    var title: String? = null
}