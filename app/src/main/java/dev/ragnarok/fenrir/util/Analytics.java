package dev.ragnarok.fenrir.util;

import dev.ragnarok.fenrir.BuildConfig;


public class Analytics {

    public static void logUnexpectedError(Throwable throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace();
        }

        //FirebaseCrash.report(throwable);
    }
}