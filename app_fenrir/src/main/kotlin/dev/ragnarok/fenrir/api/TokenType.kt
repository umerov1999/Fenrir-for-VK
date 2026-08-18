package dev.ragnarok.fenrir.api

import androidx.annotation.IntDef

@IntDef(
    TokenType.USER,
    TokenType.COMMUNITY,
    TokenType.SERVICE
)
@Retention(AnnotationRetention.SOURCE)
annotation class TokenType {
    companion object {
        const val USER = 0
        const val COMMUNITY = 1
        const val SERVICE = 2
    }
}