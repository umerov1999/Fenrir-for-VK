package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class CountersDto {
    @SerializedName("friends")
    var friends = 0

    @SerializedName("messages")
    var messages = 0

    @SerializedName("photos")
    var photos = 0

    @SerializedName("videos")
    var videos = 0

    @SerializedName("notes")
    var notes = 0

    @SerializedName("gifts")
    var gifts = 0

    @SerializedName("events")
    var events = 0

    @SerializedName("groups")
    var groups = 0

    @SerializedName("notifications")
    var notifications = 0

    @SerializedName("sdk")
    var sdk = 0

    @SerializedName("app_requests")
    var app_requests = 0
}