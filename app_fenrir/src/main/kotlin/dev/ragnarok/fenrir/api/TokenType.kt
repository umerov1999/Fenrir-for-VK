package dev.ragnarok.fenrir.api

import androidx.annotation.IntDef

@IntDef(
    TokenType.USER,
    TokenType.COMMUNITY
)
@Retention(AnnotationRetention.SOURCE)
annotation class TokenType {
    companion object {
        const val USER = 0
        const val COMMUNITY = 1
        //const val SERVICE = 2

        fun getString(@TokenType vararg tokenPolicy: Int): String {
            val str = StringBuilder("[")
            var first = true
            for (i in tokenPolicy) {
                if (first) {
                    first = false
                } else {
                    str.append(", ")
                }
                str.append(
                    when (i) {
                        USER -> "user"
                        COMMUNITY -> "community"
                        else -> "unknow"
                    }
                )
            }
            str.append("]")
            return str.toString()
        }
    }
}