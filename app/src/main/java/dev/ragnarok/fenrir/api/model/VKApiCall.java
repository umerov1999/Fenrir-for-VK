package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiCall implements VKApiAttachment {

    @SerializedName("initiator_id")
    public int initiator_id;

    @SerializedName("receiver_id")
    public int receiver_id;

    @SerializedName("state")
    public String state;

    @SerializedName("time")
    public long time;

    @Override
    public String getType() {
        return TYPE_CALL;
    }
}
