package dev.ragnarok.fenrir.settings;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class VkPushRegistration {

    @SerializedName("userId")
    private int userId;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("vkToken")
    private String vkToken;

    @SerializedName("gmcToken")
    private String gmcToken;

    public VkPushRegistration set(int userId, @NonNull String deviceId, @NonNull String vkToken, @NonNull String gmcToken) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.vkToken = vkToken;
        this.gmcToken = gmcToken;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public String getVkToken() {
        return vkToken;
    }

    public String getGmcToken() {
        return gmcToken;
    }

    public String getDeviceId() {
        return deviceId;
    }
}