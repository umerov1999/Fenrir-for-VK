package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef

@IntDef(
    AuthFlow.VALIDATE_ACCOUNT,
    AuthFlow.SELECT_VALIDATION_METHOD,
    AuthFlow.CODE_VALIDATION,
    AuthFlow.PASSWORD,
    AuthFlow.DO_AUTH
)
@Retention(AnnotationRetention.SOURCE)
annotation class AuthFlow {
    companion object {
        const val VALIDATE_ACCOUNT = 0
        const val SELECT_VALIDATION_METHOD = 1
        const val CODE_VALIDATION = 2
        const val PASSWORD = 3
        const val DO_AUTH = 4
    }
}
