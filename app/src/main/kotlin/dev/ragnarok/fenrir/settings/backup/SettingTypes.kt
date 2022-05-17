package dev.ragnarok.fenrir.settings.backup

import androidx.annotation.IntDef

@IntDef(
    SettingTypes.TYPE_BOOL,
    SettingTypes.TYPE_INT,
    SettingTypes.TYPE_STRING,
    SettingTypes.TYPE_STRING_SET
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class SettingTypes {
    companion object {
        const val TYPE_BOOL = 0
        const val TYPE_INT = 1
        const val TYPE_STRING = 2
        const val TYPE_STRING_SET = 3
    }
}