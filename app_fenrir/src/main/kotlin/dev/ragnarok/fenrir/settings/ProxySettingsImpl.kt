package dev.ragnarok.fenrir.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.model.ProxyConfig
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.createPublishSubject
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.myEmit
import kotlinx.coroutines.flow.SharedFlow

class ProxySettingsImpl(context: Context) : IProxySettings {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun put(address: String, port: Int) {
        val id = generateNextId()
        val config = ProxyConfig().set(id, address, port)
        put(config)
    }

    private fun put(config: ProxyConfig) {
        val set: MutableSet<String> =
            HashSet(preferences.getStringSet(KEY_LIST, HashSet(0)) ?: return)
        set.add(kJson.encodeToString(ProxyConfig.serializer(), config))
        preferences.edit {
            putStringSet(KEY_LIST, set)
        }
        observeAdding.myEmit(config)
    }

    override fun put(address: String, port: Int, username: String, pass: String) {
        val id = generateNextId()
        val config = ProxyConfig().set(id, address, port).setAuth(username, pass)
        put(config)
    }

    override val observeAdding: SharedFlow<ProxyConfig>
        field = createPublishSubject<ProxyConfig>()

    override val observeRemoving: SharedFlow<ProxyConfig>
        field = createPublishSubject<ProxyConfig>()

    override val observeActive: SharedFlow<Optional<ProxyConfig>>
        field = createPublishSubject<Optional<ProxyConfig>>()

    override val all: MutableList<ProxyConfig>
        get() {
            val set = preferences.getStringSet(KEY_LIST, HashSet(0))!!
            val configs: MutableList<ProxyConfig> = ArrayList(
                set.size
            )
            for (s in set) {
                configs.add(kJson.decodeFromString(ProxyConfig.serializer(), s))
            }
            return configs
        }
    override val activeProxy: ProxyConfig?
        get() {
            val active = preferences.getString(KEY_ACTIVE, null)
            return if (active.nonNullNoEmpty()) kJson.decodeFromString(
                ProxyConfig.serializer(),
                active
            ) else null
        }

    override fun setActive(config: ProxyConfig?) {
        preferences.edit {
            putString(
                KEY_ACTIVE,
                if (config == null) null else kJson.encodeToString(ProxyConfig.serializer(), config)
            )
        }
        observeActive.myEmit(wrap(config))
    }

    override fun broadcastUpdate(config: ProxyConfig?) {
        if (config == null) {
            observeActive.myEmit(
                wrap(
                    activeProxy
                )
            )
        } else {
            observeActive.myEmit(wrap(config))
        }
    }

    override fun delete(config: ProxyConfig) {
        val set: MutableSet<String> =
            HashSet(preferences.getStringSet(KEY_LIST, HashSet(0)) ?: return)
        set.remove(kJson.encodeToString(ProxyConfig.serializer(), config))
        preferences.edit {
            putStringSet(KEY_LIST, set)
        }
        observeRemoving.myEmit(config)
    }

    private fun generateNextId(): Int {
        val next = preferences.getInt(KEY_NEXT_ID, 1)
        preferences.edit {
            putInt(KEY_NEXT_ID, next + 1)
        }
        return next
    }

    companion object {
        private const val PREF_NAME = "proxy_settings"
        private const val KEY_NEXT_ID = "next_id"
        private const val KEY_LIST = "list"
        private const val KEY_ACTIVE = "active_proxy"
    }

}
