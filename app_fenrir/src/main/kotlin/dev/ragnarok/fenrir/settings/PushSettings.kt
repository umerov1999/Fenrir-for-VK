package dev.ragnarok.fenrir.settings

import android.content.Context
import androidx.core.content.edit
import de.maxr1998.modernpreferences.PreferenceScreen.Companion.getPreferences
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.ISettings.IPushSettings

internal class PushSettings(context: Context) : IPushSettings {
    private val app: Context = context.applicationContext

    override var registered: VKPushRegistration?
        get() {
            val str = getPreferences(app)
                .getString(KEY_REGISTERED, null)
            if (str.nonNullNoEmpty()) {
                try {
                    return kJson.decodeFromString(VKPushRegistration.serializer(), str)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }
        set(value) {
            getPreferences(app)
                .edit {
                    if (value == null) {
                        remove(KEY_REGISTERED)
                    } else {
                        putString(
                            KEY_REGISTERED,
                            kJson.encodeToString(VKPushRegistration.serializer(), value)
                        )
                    }
                }
        }

    companion object {
        private const val KEY_REGISTERED = "fcm_push_registered_current"
    }

}