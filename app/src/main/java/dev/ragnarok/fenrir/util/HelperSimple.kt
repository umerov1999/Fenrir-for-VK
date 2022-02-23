package dev.ragnarok.fenrir.util

import de.maxr1998.modernpreferences.PreferenceScreen
import dev.ragnarok.fenrir.Injection
import dev.ragnarok.fenrir.settings.Settings

object HelperSimple {
    const val DIALOG_SEND_HELPER = "dialog_send_helper"
    const val PLAYLIST_HELPER = "playlist_helper"
    const val STORY_HELPER = "story_helper"
    const val LOLLIPOP_21 = "lollipop21"
    const val AUDIO_DEAD = "audio_dead"
    fun needHelp(key: String, count: Int): Boolean {
        val app = Injection.provideApplicationContext()
        val ret = PreferenceScreen.getPreferences(app).getInt(key, 0)
        if (ret < count) {
            PreferenceScreen.getPreferences(app).edit().putInt(key, ret + 1).apply()
            return true
        }
        return false
    }

    fun hasHelp(key: String, count: Int): Boolean {
        val app = Injection.provideApplicationContext()
        return PreferenceScreen.getPreferences(app).getInt(key, 0) < count
    }

    fun countHelp(key: String): Int {
        val app = Injection.provideApplicationContext()
        return PreferenceScreen.getPreferences(app).getInt(key, 0)
    }

    fun toggleHelp(key: String, count: Int) {
        val app = Injection.provideApplicationContext()
        val ret = PreferenceScreen.getPreferences(app).getInt(key, 0)
        if (ret < count) {
            PreferenceScreen.getPreferences(app).edit().putInt(key, ret + 1).apply()
        }
    }

    fun hasAccountHelp(key: String): Boolean {
        val fullKey = key + "_" + Settings.get().accounts().current
        val app = Injection.provideApplicationContext()
        return PreferenceScreen.getPreferences(app).getBoolean(fullKey, true)
    }

    fun toggleAccountHelp(key: String) {
        val fullKey = key + "_" + Settings.get().accounts().current
        val app = Injection.provideApplicationContext()
        PreferenceScreen.getPreferences(app).edit().putBoolean(fullKey, false).apply()
    }
}
