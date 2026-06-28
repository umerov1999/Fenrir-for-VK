package dev.ragnarok.fenrir

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

object FCMRegistrationUtils {
    val register: Flow<Boolean>
        get() = flow {
            FirebaseMessaging.getInstance().register().await()
            emit(true)
        }

    val unregister: Flow<Boolean>
        get() = flow {
            FirebaseMessaging.getInstance().unregister().await()
            emit(true)
        }
}