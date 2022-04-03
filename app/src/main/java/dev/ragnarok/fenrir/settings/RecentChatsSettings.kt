package dev.ragnarok.fenrir.settings

import android.content.Context
import com.google.gson.Gson
import de.maxr1998.modernpreferences.PreferenceScreen.Companion.getPreferences
import dev.ragnarok.fenrir.model.drawer.RecentChat
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.ISettings.IRecentChats

internal class RecentChatsSettings(app: Context) : IRecentChats {
    private val app: Context = app.applicationContext
    private val gson: Gson = Gson()
    override fun get(acountid: Int): MutableList<RecentChat> {
        val recentChats: MutableList<RecentChat> = ArrayList()
        val stringSet = getPreferences(app)
            .getStringSet(recentChatKeyFor(acountid), null)
        if (stringSet.nonNullNoEmpty()) {
            for (s in stringSet) {
                try {
                    val recentChat = gson.fromJson(s, RecentChat::class.java)
                    recentChats.add(recentChat)
                } catch (ignored: Exception) {
                }
            }
        }
        return recentChats
    }

    override fun store(accountid: Int, chats: List<RecentChat?>) {
        val target: MutableSet<String> = LinkedHashSet()
        for (item in chats) {
            if (item != null) {
                if (item.aid != accountid) continue
                target.add(gson.toJson(item))
            }
        }
        getPreferences(app)
            .edit()
            .putStringSet(recentChatKeyFor(accountid), target)
            .apply()
    }

    companion object {
        private fun recentChatKeyFor(aid: Int): String {
            return "recent$aid"
        }
    }

}