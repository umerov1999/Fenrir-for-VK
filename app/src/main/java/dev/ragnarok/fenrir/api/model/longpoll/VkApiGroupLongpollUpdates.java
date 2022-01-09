package dev.ragnarok.fenrir.api.model.longpoll;

import com.google.gson.annotations.SerializedName;

public final class VkApiGroupLongpollUpdates {

    @SerializedName("failed")
    public int failed;

    @SerializedName("ts")
    public String ts;

    public int getCount() {
        return 0;
    }
}