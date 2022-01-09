package dev.ragnarok.fenrir.settings;

import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.content.Context;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.model.drawer.AbsMenuItem;
import dev.ragnarok.fenrir.model.drawer.RecentChat;

class RecentChatsSettings implements ISettings.IRecentChats {

    private final Context app;

    private final Gson gson;

    RecentChatsSettings(Context app) {
        this.app = app.getApplicationContext();
        gson = new Gson();
    }

    private static String recentChatKeyFor(int aid) {
        return "recent" + aid;
    }

    @Override
    public List<RecentChat> get(int acountid) {
        List<RecentChat> recentChats = new ArrayList<>();

        Set<String> stringSet = PreferenceManager.getDefaultSharedPreferences(app)
                .getStringSet(recentChatKeyFor(acountid), null);

        if (!safeIsEmpty(stringSet)) {
            for (String s : stringSet) {
                try {
                    RecentChat recentChat = gson.fromJson(s, RecentChat.class);
                    recentChats.add(recentChat);
                } catch (Exception ignored) {
                }
            }
        }

        return recentChats;
    }

    @Override
    public void store(int accountid, List<RecentChat> chats) {
        Set<String> target = new LinkedHashSet<>();
        for (AbsMenuItem item : chats) {
            if (item instanceof RecentChat) {

                if (((RecentChat) item).getAid() != accountid) continue;

                target.add(gson.toJson(item));
            }
        }

        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putStringSet(recentChatKeyFor(accountid), target)
                .apply();
    }
}
