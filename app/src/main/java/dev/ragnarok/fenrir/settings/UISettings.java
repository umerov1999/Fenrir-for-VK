package dev.ragnarok.fenrir.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.preference.PreferenceManager;

import dev.ragnarok.fenrir.fragment.PreferencesFragment;
import dev.ragnarok.fenrir.fragment.fave.FaveTabsFragment;
import dev.ragnarok.fenrir.fragment.friends.FriendsTabsFragment;
import dev.ragnarok.fenrir.fragment.search.SearchTabsFragment;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;

class UISettings implements ISettings.IUISettings {

    private final Context app;

    UISettings(Context context) {
        app = context.getApplicationContext();
    }

    @Override
    public int getAvatarStyle() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        return preferences.getInt(PreferencesFragment.KEY_AVATAR_STYLE, AvatarStyle.CIRCLE);
    }

    @Override
    public void storeAvatarStyle(@AvatarStyle int style) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(PreferencesFragment.KEY_AVATAR_STYLE, style)
                .apply();
    }

    @Override
    public String getMainThemeKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        return preferences.getString("app_theme", "cold");
    }

    @Override
    public void setMainTheme(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        preferences.edit().putString("app_theme", key).apply();
    }

    @Override
    public void switchNightMode(@NightMode int key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        preferences.edit().putString("night_switch", String.valueOf(key)).apply();
    }

    @Override
    public boolean isDarkModeEnabled(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    @NightMode
    @Override
    public int getNightMode() {
        try {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(app)
                    .getString("night_switch", String.valueOf(NightMode.ENABLE)).trim());
        } catch (Exception e) {
            return NightMode.ENABLE;
        }
    }

    @Override
    public Place getDefaultPage(int accountId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        String page = preferences.getString(PreferencesFragment.KEY_DEFAULT_CATEGORY, "last_closed");

        if ("last_closed".equals(page)) {
            int type = PreferenceManager.getDefaultSharedPreferences(app).getInt("last_closed_place_type", Place.DIALOGS);
            switch (type) {
                case Place.DIALOGS:
                    return PlaceFactory.getDialogsPlace(accountId, accountId, null);
                case Place.FEED:
                    return PlaceFactory.getFeedPlace(accountId);
                case Place.FRIENDS_AND_FOLLOWERS:
                    return PlaceFactory.getFriendsFollowersPlace(accountId, accountId, FriendsTabsFragment.TAB_ALL_FRIENDS, null);
                case Place.NOTIFICATIONS:
                    return PlaceFactory.getNotificationsPlace(accountId);
                case Place.NEWSFEED_COMMENTS:
                    return PlaceFactory.getNewsfeedCommentsPlace(accountId);
                case Place.COMMUNITIES:
                    return PlaceFactory.getCommunitiesPlace(accountId, accountId);
                case Place.VK_PHOTO_ALBUMS:
                    return PlaceFactory.getVKPhotoAlbumsPlace(accountId, accountId, null, null);
                case Place.AUDIOS:
                    return PlaceFactory.getAudiosPlace(accountId, accountId);
                case Place.DOCS:
                    return PlaceFactory.getDocumentsPlace(accountId, accountId, null);
                case Place.BOOKMARKS:
                    return PlaceFactory.getBookmarksPlace(accountId, FaveTabsFragment.TAB_PAGES);
                case Place.SEARCH:
                    return PlaceFactory.getSearchPlace(accountId, SearchTabsFragment.TAB_PEOPLE);
                case Place.VIDEOS:
                    return PlaceFactory.getVideosPlace(accountId, accountId, null);
                case Place.PREFERENCES:
                    return PlaceFactory.getPreferencesPlace(accountId);
            }
        }

        switch (page) {
            case "1":
                return PlaceFactory.getFriendsFollowersPlace(accountId, accountId, FriendsTabsFragment.TAB_ALL_FRIENDS, null);
            case "3":
                return PlaceFactory.getFeedPlace(accountId);
            case "4":
                return PlaceFactory.getNotificationsPlace(accountId);
            case "5":
                return PlaceFactory.getCommunitiesPlace(accountId, accountId);
            case "6":
                return PlaceFactory.getVKPhotoAlbumsPlace(accountId, accountId, null, null);
            case "7":
                return PlaceFactory.getVideosPlace(accountId, accountId, null);
            case "8":
                return PlaceFactory.getAudiosPlace(accountId, accountId);
            case "9":
                return PlaceFactory.getDocumentsPlace(accountId, accountId, null);
            case "10":
                return PlaceFactory.getBookmarksPlace(accountId, FaveTabsFragment.TAB_PAGES);
            case "11":
                return PlaceFactory.getSearchPlace(accountId, SearchTabsFragment.TAB_PEOPLE);
            case "12":
                return PlaceFactory.getNewsfeedCommentsPlace(accountId);
            default:
                return PlaceFactory.getDialogsPlace(accountId, accountId, null);
        }
    }

    @Override
    public void notifyPlaceResumed(int type) {
        PreferenceManager.getDefaultSharedPreferences(app).edit()
                .putInt("last_closed_place_type", type)
                .apply();
    }

    @Override
    public boolean isSystemEmoji() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("emojis_type", false);
    }

    @Override
    public boolean isEmojis_full_screen() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("emojis_full_screen", false);
    }

    @Override
    public boolean isStickers_by_theme() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("stickers_by_theme", true);
    }

    @Override
    public boolean isStickers_by_new() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("stickers_by_new", false);
    }

    @Override
    public int isPhoto_swipe_triggered_pos() {
        try {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(app).getString("photo_swipe_triggered_pos", "180").trim());
        } catch (Exception e) {
            return 180;
        }
    }

    @Override
    public boolean isShow_profile_in_additional_page() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("show_profile_in_additional_page", true);
    }

    @SwipesChatMode
    @Override
    public int getSwipes_chat_mode() {
        try {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(app).getString("swipes_for_chats", "1").trim());
        } catch (Exception e) {
            return SwipesChatMode.SLIDR;
        }
    }

    @Override
    public boolean isDisplay_writing() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("display_writing", true);
    }
}
