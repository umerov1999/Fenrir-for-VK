package dev.ragnarok.fenrir;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.messaging.FirebaseMessaging;

import io.reactivex.rxjava3.core.Single;

public class FCMToken {
    public static Single<String> getFcmToken() {
        return Single.create(emitter -> {
            OnCompleteListener<String> listener = task -> {
                if (task.isSuccessful()) {
                    emitter.onSuccess(task.getResult());
                } else {
                    emitter.tryOnError(task.getException());
                }
            };

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(listener);
        });
    }
}
