package dev.ragnarok.filegallery.settings

import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatDelegate

@IntDef(NightMode.FOLLOW_SYSTEM, NightMode.DISABLE, NightMode.ENABLE, NightMode.AUTO)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class NightMode {
    companion object {
        const val DISABLE = AppCompatDelegate.MODE_NIGHT_NO
        const val ENABLE = AppCompatDelegate.MODE_NIGHT_YES
        const val AUTO = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        const val FOLLOW_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}