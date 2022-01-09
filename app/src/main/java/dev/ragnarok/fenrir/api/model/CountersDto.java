package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class CountersDto {

    @SerializedName("friends")
    public int friends;

    @SerializedName("messages")
    public int messages;

    @SerializedName("photos")
    public int photos;

    @SerializedName("videos")
    public int videos;

    @SerializedName("notes")
    public int notes;

    @SerializedName("gifts")
    public int gifts;

    @SerializedName("events")
    public int events;

    @SerializedName("groups")
    public int groups;

    @SerializedName("notifications")
    public int notifications;

    @SerializedName("sdk")
    public int sdk;

    @SerializedName("app_requests")
    public int app_requests;
}
