package dev.ragnarok.fenrir.util;

import android.text.TextUtils;
import android.util.Log;

import dev.ragnarok.fenrir.Constants;

public class Exestime {

    private static final String TAG = Exestime.class.getSimpleName();

    public static void log(String method, long startTime, Object... params) {
        if (!Constants.IS_DEBUG) return;

        if (params == null || params.length == 0) {
            Log.d(TAG, method + ", time: " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
            Log.d(TAG, method + ", time: " + (System.currentTimeMillis() - startTime) + " ms, params: [" + TextUtils.join(", ", params) + "]");
        }
    }
}
