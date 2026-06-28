package dev.ragnarok.fenrir.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VKPushRegistration {
    @SerialName("userId")
    var userId = 0L
        private set

    @SerialName("deviceId")
    lateinit var deviceId: String
        private set

    @SerialName("fcmToken")
    lateinit var fcmToken: String
        private set

    fun set(
        userId: Long,
        deviceId: String,
        fcmToken: String
    ): VKPushRegistration {
        this.userId = userId
        this.deviceId = deviceId
        this.fcmToken = fcmToken
        return this
    }
}