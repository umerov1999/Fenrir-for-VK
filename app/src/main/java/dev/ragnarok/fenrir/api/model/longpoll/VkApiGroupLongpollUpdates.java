package dev.ragnarok.fenrir.api.model.longpoll;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public final class VkApiGroupLongpollUpdates {

    @SerializedName("failed")
    public int failed;

    @Nullable
    @SerializedName("ts")
    public String ts;

    public int getCount() {
        return 0;
    }
}