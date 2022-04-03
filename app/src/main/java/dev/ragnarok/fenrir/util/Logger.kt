package dev.ragnarok.fenrir.util

import android.util.Log
import dev.ragnarok.fenrir.Constants

object Logger {
    @JvmStatic
    fun i(tag: String?, message: String) {
        if (Constants.IS_DEBUG) {
            Log.i(tag, message)
        }
    }

    @JvmStatic
    fun d(tag: String?, message: String) {
        if (Constants.IS_DEBUG) {
            Log.d(tag, message)
        }
    }

    @JvmStatic
    fun e(tag: String?, message: String) {
        if (Constants.IS_DEBUG) {
            Log.e(tag, message)
        }
    }

    @JvmStatic
    fun wtf(tag: String?, message: String) {
        if (Constants.IS_DEBUG) {
            Log.wtf(tag, message)
        }
    }
}