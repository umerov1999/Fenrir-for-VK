package dev.ragnarok.fenrir.push;

import java.util.concurrent.Executors;

import dev.ragnarok.fenrir.Injection;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotificationScheduler {

    public static final Scheduler INSTANCE = Schedulers.from(Executors.newFixedThreadPool(1));

    public static <T> SingleTransformer<T, T> fromNotificationThreadToMain() {
        return single -> single
                .subscribeOn(INSTANCE)
                .observeOn(Injection.provideMainThreadScheduler());
    }
}