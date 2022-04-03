package dev.ragnarok.fenrir.util

import dev.ragnarok.fenrir.Constants

object Analytics {

    fun logUnexpectedError(throwable: Throwable?) {
        if (Constants.IS_DEBUG) {
            throwable?.printStackTrace()
        }

        //FirebaseCrash.report(throwable)
    }
}