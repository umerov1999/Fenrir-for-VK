package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiGroupChats {

    @SerializedName("id")
    public int id;

    @SerializedName("members_count")
    public int members_count;

    @SerializedName("last_message_date")
    public long last_message_date;

    @SerializedName("is_closed")
    public boolean is_closed;

    @SerializedName("invite_link")
    public String invite_link;

    @SerializedName("photo")
    public String photo;

    @SerializedName("title")
    public String title;
}
