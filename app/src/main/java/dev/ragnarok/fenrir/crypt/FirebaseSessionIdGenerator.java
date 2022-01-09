package dev.ragnarok.fenrir.crypt;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import dev.ragnarok.fenrir.BuildConfig;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Single;

public class FirebaseSessionIdGenerator implements ISessionIdGenerator {

    @Override
    public Single<Long> generateNextId() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(BuildConfig.FCM_SESSION_ID_GEN_URL);
        DatabaseReference ref = database.getReference();
        DatabaseReference databaseCounter = ref.child("key_exchange_session_counter");

        //https://stackoverflow.com/questions/28915706/auto-increment-a-value-in-firebase
        return Single.create(emitter -> databaseCounter.runTransaction(new Transaction.Handler() {

            long nextValue;

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    nextValue = 1;
                } else {
                    nextValue = (Long) currentData.getValue() + 1;
                }

                currentData.setValue(nextValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean committed, DataSnapshot currentData) {
                if (Objects.nonNull(e)) {
                    emitter.onError(new SessionIdGenerationException(e.getMessage()));
                } else {
                    emitter.onSuccess(nextValue);
                }
            }
        }));
    }
}
