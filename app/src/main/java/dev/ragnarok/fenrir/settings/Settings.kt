package dev.ragnarok.fenrir.settings

import dev.ragnarok.fenrir.Includes.settings

object Settings {
    @JvmStatic
    fun get(): ISettings {
        return settings
    }
}