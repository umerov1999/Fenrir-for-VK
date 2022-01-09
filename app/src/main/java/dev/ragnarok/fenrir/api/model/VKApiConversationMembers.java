package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiConversationMembers {
    @SerializedName("member_id")
    public int member_id;

    @SerializedName("invited_by")
    public int invited_by;

    @SerializedName("join_date")
    public long join_date;

    @SerializedName("is_admin")
    public boolean is_admin;

    @SerializedName("is_owner")
    public boolean is_owner;

    @SerializedName("can_kick")
    public boolean can_kick;
}
