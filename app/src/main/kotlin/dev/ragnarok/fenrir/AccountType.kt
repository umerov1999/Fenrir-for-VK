package dev.ragnarok.fenrir

import androidx.annotation.IntDef

@IntDef(
    AccountType.BY_TYPE,
    AccountType.VK_ANDROID,
    AccountType.VK_ANDROID_HIDDEN,
    AccountType.KATE,
    AccountType.KATE_HIDDEN
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class AccountType {
    companion object {
        const val BY_TYPE = 0
        const val VK_ANDROID = 1
        const val VK_ANDROID_HIDDEN = 2
        const val KATE = 3
        const val KATE_HIDDEN = 4
    }
}