package dev.ragnarok.fenrir.util;

import dev.ragnarok.fenrir.Constants;


public class Analytics {

    public static void logUnexpectedError(Throwable throwable) {
        if (Constants.IS_DEBUG) {
            throwable.printStackTrace();
        }

        //FirebaseCrash.report(throwable);
    }
}