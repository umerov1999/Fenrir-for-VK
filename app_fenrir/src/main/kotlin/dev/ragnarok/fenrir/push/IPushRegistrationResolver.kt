package dev.ragnarok.fenrir.push

import android.content.Context
import dev.ragnarok.fenrir.settings.VKPushRegistration
import kotlinx.coroutines.flow.Flow

interface IPushRegistrationResolver {
    fun canReceivePushNotification(accountId: Long): Boolean
    fun resolvePushRegistration(fcmToken: String, context: Context): Flow<Boolean>
    fun unregister(registration: VKPushRegistration): Flow<Boolean>
}