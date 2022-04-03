package dev.ragnarok.fenrir.util

import android.annotation.SuppressLint
import dev.ragnarok.fenrir.Includes.logsStore
import dev.ragnarok.fenrir.model.LogEvent
import dev.ragnarok.fenrir.settings.Settings
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.PrintWriter
import java.io.StringWriter

object PersistentLogger {
    @JvmStatic
    @SuppressLint("CheckResult")
    fun logThrowable(tag: String, throwable: Throwable?) {
        if (!Settings.get().other().isDoLogs) return
        val store = logsStore
        val cause = Utils.getCauseIfRuntime(throwable)
        getStackTrace(cause)
            .flatMapCompletable { s: String ->
                store.add(LogEvent.Type.ERROR, tag, s)
                    .ignoreElement()
            }
            .onErrorComplete()
            .subscribeOn(Schedulers.io())
            .subscribe({}) { }
    }

    private fun getStackTrace(throwable: Throwable): Single<String> {
        return Single.fromCallable {
            StringWriter().use { sw ->
                PrintWriter(sw).use { pw ->
                    throwable.printStackTrace(pw)
                    return@fromCallable sw.toString()
                }
            }
        }
    }
}