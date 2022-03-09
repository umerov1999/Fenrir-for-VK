package dev.ragnarok.fenrir.util;

import android.util.Log;

import dev.ragnarok.fenrir.Constants;

public class Logger {
    public static void i(String tag, String message) {
        if (Constants.IS_DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (Constants.IS_DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (Constants.IS_DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void wtf(String tag, String message) {
        if (Constants.IS_DEBUG) {
            Log.wtf(tag, message);
        }
    }
}
