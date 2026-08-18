package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef

@IntDef(
    DrawerType.ADDITIONAL,
    DrawerType.SIDE,
    DrawerType.BOTTOM
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class DrawerType {
    companion object {
        const val ADDITIONAL = 0
        const val SIDE = 1
        const val BOTTOM = 2
    }
}