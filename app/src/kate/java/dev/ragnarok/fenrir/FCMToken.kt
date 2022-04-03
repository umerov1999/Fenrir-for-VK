package dev.ragnarok.fenrir

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

object FCMToken {
    val fcmToken: Single<String>
        get() = Single.create { emitter: SingleEmitter<String> ->
            val listener = OnCompleteListener { task: Task<InstanceIdResult> ->
                if (task.isSuccessful) {
                    val result = task.result
                    emitter.onSuccess(result.token)
                } else {
                    emitter.tryOnError(task.exception ?: Throwable("fcmToken Kate!!!"))
                }
            }
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(listener)
        }
}