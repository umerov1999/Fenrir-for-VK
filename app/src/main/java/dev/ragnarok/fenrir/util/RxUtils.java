package dev.ragnarok.fenrir.util;

import androidx.annotation.Nullable;

import java.io.Closeable;

import dev.ragnarok.fenrir.BuildConfig;
import dev.ragnarok.fenrir.Injection;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableTransformer;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.MaybeTransformer;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RxUtils {

    private static final Action DUMMMY_ACTION_0 = () -> {
    };

    public static Action dummy() {
        return DUMMMY_ACTION_0;
    }

    public static <T> Consumer<T> ignore() {
        return t -> {
            if (t instanceof Throwable && BuildConfig.DEBUG) {
                ((Throwable) t).printStackTrace();
            }
        };
    }

    public static Action safelyCloseAction(@Nullable Closeable closeable) {
        return () -> Utils.safelyClose(closeable);
    }

    public static <T> Disposable subscribeOnIOAndIgnore(Single<T> single) {
        return single.subscribeOn(Schedulers.io())
                .subscribe(ignore(), ignore());
    }

    public static Disposable subscribeOnIOAndIgnore(Completable completable) {
        return completable.subscribeOn(Schedulers.io())
                .subscribe(dummy(), ignore());
    }

    public static <T> MaybeTransformer<T, T> applyMaybeIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> SingleTransformer<T, T> applySingleIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> SingleTransformer<T, T> applySingleComputationToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.computation())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> ObservableTransformer<T, T> applyObservableIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> FlowableTransformer<T, T> applyFlowableIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.computation())
                .observeOn(Injection.provideMainThreadScheduler());
    }


    public static CompletableTransformer applyCompletableIOToMainSchedulers() {
        return completable -> completable.subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> T BlockingGetSingle(Single<T> single, T default_value) {
        try {
            return single.blockingGet();
        } catch (Throwable ignored) {
        }
        return default_value;
    }
}
