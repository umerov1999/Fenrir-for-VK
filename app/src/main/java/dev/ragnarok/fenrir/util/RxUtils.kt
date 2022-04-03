package dev.ragnarok.fenrir.util

import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.Closeable

object RxUtils {
    private val DUMMMY_ACTION_0 = Action {}

    @JvmStatic
    fun dummy(): Action {
        return DUMMMY_ACTION_0
    }

    @JvmStatic
    fun <T : Any> ignore(): Consumer<T> {
        return Consumer { t: T ->
            if (t is Throwable && Constants.IS_DEBUG) {
                (t as Throwable).printStackTrace()
            }
        }
    }


    fun safelyCloseAction(closeable: Closeable?): Action {
        return Action { Utils.safelyClose(closeable) }
    }


    fun <T : Any> subscribeOnIOAndIgnore(single: Single<T>): Disposable {
        return single.subscribeOn(Schedulers.io())
            .subscribe(ignore(), ignore())
    }


    fun subscribeOnIOAndIgnore(completable: Completable): Disposable {
        return completable.subscribeOn(Schedulers.io())
            .subscribe(dummy(), ignore())
    }

    @JvmStatic
    fun <T : Any> applyMaybeIOToMainSchedulers(): MaybeTransformer<T, T> {
        return MaybeTransformer { upstream: Maybe<T> ->
            upstream
                .subscribeOn(Schedulers.io())
                .observeOn(provideMainThreadScheduler())
        }
    }

    @JvmStatic
    fun <T : Any> applySingleIOToMainSchedulers(): SingleTransformer<T, T> {
        return SingleTransformer { upstream: Single<T> ->
            upstream
                .subscribeOn(Schedulers.io())
                .observeOn(provideMainThreadScheduler())
        }
    }

    @JvmStatic
    fun <T : Any> applySingleComputationToMainSchedulers(): SingleTransformer<T, T> {
        return SingleTransformer { upstream: Single<T> ->
            upstream
                .subscribeOn(Schedulers.computation())
                .observeOn(provideMainThreadScheduler())
        }
    }

    @JvmStatic
    fun <T : Any> applyObservableIOToMainSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream: Observable<T> ->
            upstream
                .subscribeOn(Schedulers.io())
                .observeOn(provideMainThreadScheduler())
        }
    }

    @JvmStatic
    fun <T : Any> applyFlowableIOToMainSchedulers(): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream: Flowable<T> ->
            upstream
                .subscribeOn(Schedulers.computation())
                .observeOn(provideMainThreadScheduler())
        }
    }

    @JvmStatic
    fun applyCompletableIOToMainSchedulers(): CompletableTransformer {
        return CompletableTransformer { completable: Completable ->
            completable.subscribeOn(
                Schedulers.io()
            )
                .observeOn(provideMainThreadScheduler())
        }
    }

    @JvmStatic
    fun <T : Any> blockingGetSingle(single: Single<T>, default_value: T?): T? {
        try {
            return single.blockingGet()
        } catch (ignored: Throwable) {
        }
        return default_value
    }
}