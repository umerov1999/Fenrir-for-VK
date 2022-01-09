package dev.ragnarok.fenrir;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.reactivex.rxjava3.core.Single;

public class FCMToken {
    public static Single<String> getFcmToken() {
        return Single.create(emitter -> {
            OnCompleteListener<InstanceIdResult> listener = task -> {
                if (task.isSuccessful()) {
                    InstanceIdResult result = task.getResult();
                    emitter.onSuccess(result.getToken());
                } else {
                    emitter.tryOnError(task.getException());
                }
            };

            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(listener);
        });
    }
}
