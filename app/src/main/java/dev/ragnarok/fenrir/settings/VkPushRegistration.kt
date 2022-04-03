package dev.ragnarok.fenrir.settings

import com.google.gson.annotations.SerializedName

class VkPushRegistration {
    @SerializedName("userId")
    var userId = 0
        private set

    @SerializedName("deviceId")
    lateinit var deviceId: String
        private set

    @SerializedName("vkToken")
    lateinit var vkToken: String
        private set

    @SerializedName("gmcToken")
    lateinit var gmcToken: String
        private set

    operator fun set(
        userId: Int,
        deviceId: String,
        vkToken: String,
        gmcToken: String
    ): VkPushRegistration {
        this.userId = userId
        this.deviceId = deviceId
        this.vkToken = vkToken
        this.gmcToken = gmcToken
        return this
    }
}