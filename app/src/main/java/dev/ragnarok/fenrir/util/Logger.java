package dev.ragnarok.fenrir.util;

import android.util.Log;

import dev.ragnarok.fenrir.BuildConfig;

public class Logger {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void i(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void wtf(String tag, String message) {
        if (DEBUG) {
            Log.wtf(tag, message);
        }
    }
}
