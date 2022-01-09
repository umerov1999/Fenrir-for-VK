package dev.ragnarok.fenrir.util;

import android.text.TextUtils;
import android.util.Log;

import dev.ragnarok.fenrir.BuildConfig;

public class Exestime {

    private static final String TAG = Exestime.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void log(String method, long startTime, Object... params) {
        if (!DEBUG) return;

        if (params == null || params.length == 0) {
            Log.d(TAG, method + ", time: " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
            Log.d(TAG, method + ", time: " + (System.currentTimeMillis() - startTime) + " ms, params: [" + TextUtils.join(", ", params) + "]");
        }
    }
}
