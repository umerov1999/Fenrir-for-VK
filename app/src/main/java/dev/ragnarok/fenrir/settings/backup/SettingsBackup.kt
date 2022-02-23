package dev.ragnarok.fenrir.settings.backup

import android.content.SharedPreferences
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.maxr1998.modernpreferences.PreferenceScreen
import dev.ragnarok.fenrir.Injection
import dev.ragnarok.fenrir.fragment.PreferencesFragment
import dev.ragnarok.fenrir.settings.Settings

class SettingsBackup {
    private val settings: Array<SettingCollector> = arrayOf(
        //Main
        SettingCollector("send_by_enter", SettingTypes.TYPE_BOOL),
        SettingCollector("theme_overlay", SettingTypes.TYPE_STRING),
        SettingCollector("audio_round_icon", SettingTypes.TYPE_BOOL),
        SettingCollector("use_long_click_download", SettingTypes.TYPE_BOOL),
        SettingCollector("revert_play_audio", SettingTypes.TYPE_BOOL),
        SettingCollector("is_player_support_volume", SettingTypes.TYPE_BOOL),
        SettingCollector("show_bot_keyboard", SettingTypes.TYPE_BOOL),
        SettingCollector("my_message_no_color", SettingTypes.TYPE_BOOL),
        SettingCollector("notification_bubbles", SettingTypes.TYPE_BOOL),
        SettingCollector("messages_menu_down", SettingTypes.TYPE_BOOL),
        SettingCollector("expand_voice_transcript", SettingTypes.TYPE_BOOL),
        SettingCollector("image_size", SettingTypes.TYPE_STRING),
        SettingCollector("start_news", SettingTypes.TYPE_STRING),
        SettingCollector("crypt_version", SettingTypes.TYPE_STRING),
        SettingCollector("photo_preview_size", SettingTypes.TYPE_STRING),
        SettingCollector("viewpager_page_transform", SettingTypes.TYPE_STRING),
        SettingCollector("player_cover_transform", SettingTypes.TYPE_STRING),
        SettingCollector("pref_display_photo_size", SettingTypes.TYPE_INT),
        SettingCollector("photo_rounded_view", SettingTypes.TYPE_STRING),
        SettingCollector("font_size", SettingTypes.TYPE_STRING),
        SettingCollector("is_open_url_internal", SettingTypes.TYPE_STRING),
        SettingCollector("webview_night_mode", SettingTypes.TYPE_BOOL),
        SettingCollector("load_history_notif", SettingTypes.TYPE_BOOL),
        SettingCollector("snow_mode", SettingTypes.TYPE_BOOL),
        SettingCollector("dont_write", SettingTypes.TYPE_BOOL),
        SettingCollector("over_ten_attach", SettingTypes.TYPE_BOOL),
        //UI
        SettingCollector(PreferencesFragment.KEY_AVATAR_STYLE, SettingTypes.TYPE_INT),
        SettingCollector("app_theme", SettingTypes.TYPE_STRING),
        SettingCollector("night_switch", SettingTypes.TYPE_STRING),
        SettingCollector(PreferencesFragment.KEY_DEFAULT_CATEGORY, SettingTypes.TYPE_STRING),
        SettingCollector("last_closed_place_type", SettingTypes.TYPE_INT),
        SettingCollector("emojis_type", SettingTypes.TYPE_BOOL),
        SettingCollector("emojis_full_screen", SettingTypes.TYPE_BOOL),
        SettingCollector("stickers_by_theme", SettingTypes.TYPE_BOOL),
        SettingCollector("stickers_by_new", SettingTypes.TYPE_BOOL),
        SettingCollector("show_profile_in_additional_page", SettingTypes.TYPE_BOOL),
        SettingCollector("swipes_for_chats", SettingTypes.TYPE_STRING),
        SettingCollector("display_writing", SettingTypes.TYPE_BOOL),
        //Other
        SettingCollector("swipes_for_chats", SettingTypes.TYPE_STRING),
        SettingCollector("broadcast", SettingTypes.TYPE_BOOL),
        SettingCollector("comments_desc", SettingTypes.TYPE_BOOL),
        SettingCollector("keep_longpoll", SettingTypes.TYPE_BOOL),
        SettingCollector("disable_error_fcm", SettingTypes.TYPE_BOOL),
        SettingCollector("settings_no_push", SettingTypes.TYPE_BOOL),
        SettingCollector("max_bitmap_resolution", SettingTypes.TYPE_STRING),
        SettingCollector("ffmpeg_audio_codecs", SettingTypes.TYPE_STRING),
        SettingCollector("lifecycle_music_service", SettingTypes.TYPE_STRING),
        SettingCollector("autoplay_gif", SettingTypes.TYPE_BOOL),
        SettingCollector("strip_news_repost", SettingTypes.TYPE_BOOL),
        SettingCollector("ad_block_story_news", SettingTypes.TYPE_BOOL),
        SettingCollector("block_news_by_words_set", SettingTypes.TYPE_STRING_SET),
        SettingCollector("new_loading_dialog", SettingTypes.TYPE_BOOL),
        SettingCollector("vk_api_domain", SettingTypes.TYPE_STRING),
        SettingCollector("vk_auth_domain", SettingTypes.TYPE_STRING),
        SettingCollector("developer_mode", SettingTypes.TYPE_BOOL),
        SettingCollector("do_logs", SettingTypes.TYPE_BOOL),
        SettingCollector("force_cache", SettingTypes.TYPE_BOOL),
        SettingCollector("disable_history", SettingTypes.TYPE_BOOL),
        SettingCollector("show_wall_cover", SettingTypes.TYPE_BOOL),
        SettingCollector("custom_chat_color", SettingTypes.TYPE_INT),
        SettingCollector("custom_chat_color_second", SettingTypes.TYPE_INT),
        SettingCollector("custom_chat_color_usage", SettingTypes.TYPE_BOOL),
        SettingCollector("custom_message_color", SettingTypes.TYPE_INT),
        SettingCollector("custom_second_message_color", SettingTypes.TYPE_INT),
        SettingCollector("custom_message_color_usage", SettingTypes.TYPE_BOOL),
        SettingCollector("info_reading", SettingTypes.TYPE_BOOL),
        SettingCollector("auto_read", SettingTypes.TYPE_BOOL),
        SettingCollector("not_update_dialogs", SettingTypes.TYPE_BOOL),
        SettingCollector("be_online", SettingTypes.TYPE_BOOL),
        SettingCollector("donate_anim_set", SettingTypes.TYPE_STRING),
        SettingCollector("use_stop_audio", SettingTypes.TYPE_BOOL),
        SettingCollector("player_has_background", SettingTypes.TYPE_BOOL),
        SettingCollector("show_mini_player", SettingTypes.TYPE_BOOL),
        SettingCollector("enable_last_read", SettingTypes.TYPE_BOOL),
        SettingCollector("not_read_show", SettingTypes.TYPE_BOOL),
        SettingCollector("headers_in_dialog", SettingTypes.TYPE_BOOL),
        SettingCollector("show_recent_dialogs", SettingTypes.TYPE_BOOL),
        SettingCollector("show_audio_top", SettingTypes.TYPE_BOOL),
        SettingCollector("use_internal_downloader", SettingTypes.TYPE_BOOL),
        SettingCollector("music_dir", SettingTypes.TYPE_STRING),
        SettingCollector("photo_dir", SettingTypes.TYPE_STRING),
        SettingCollector("video_dir", SettingTypes.TYPE_STRING),
        SettingCollector("docs_dir", SettingTypes.TYPE_STRING),
        SettingCollector("sticker_dir", SettingTypes.TYPE_STRING),
        SettingCollector("sticker_dir", SettingTypes.TYPE_STRING),
        SettingCollector("photo_to_user_dir", SettingTypes.TYPE_BOOL),
        SettingCollector("download_voice_ogg", SettingTypes.TYPE_BOOL),
        SettingCollector("delete_cache_images", SettingTypes.TYPE_BOOL),
        SettingCollector("compress_traffic", SettingTypes.TYPE_BOOL),
        SettingCollector("do_not_clear_back_stack", SettingTypes.TYPE_BOOL),
        SettingCollector("mention_fave", SettingTypes.TYPE_BOOL),
        SettingCollector("disable_encryption", SettingTypes.TYPE_BOOL),
        SettingCollector("download_photo_tap", SettingTypes.TYPE_BOOL),
        SettingCollector("audio_save_mode_button", SettingTypes.TYPE_BOOL),
        SettingCollector("show_mutual_count", SettingTypes.TYPE_BOOL),
        SettingCollector("not_friend_show", SettingTypes.TYPE_BOOL),
        SettingCollector("do_zoom_photo", SettingTypes.TYPE_BOOL),
        SettingCollector("change_upload_size", SettingTypes.TYPE_BOOL),
        SettingCollector("show_photos_line", SettingTypes.TYPE_BOOL),
        SettingCollector("do_auto_play_video", SettingTypes.TYPE_BOOL),
        SettingCollector("video_controller_to_decor", SettingTypes.TYPE_BOOL),
        SettingCollector("video_swipes", SettingTypes.TYPE_BOOL),
        SettingCollector("disable_likes", SettingTypes.TYPE_BOOL),
        SettingCollector("disable_notifications", SettingTypes.TYPE_BOOL),
        SettingCollector("native_parcel_photo", SettingTypes.TYPE_BOOL),
        SettingCollector("native_parcel_story", SettingTypes.TYPE_BOOL),
        SettingCollector("extra_debug", SettingTypes.TYPE_BOOL),
        SettingCollector("dump_fcm", SettingTypes.TYPE_BOOL),
        SettingCollector("hint_stickers", SettingTypes.TYPE_BOOL),
        SettingCollector("enable_native", SettingTypes.TYPE_BOOL),
        SettingCollector("enable_cache_ui_anim", SettingTypes.TYPE_BOOL),
        SettingCollector("disable_sensored_voice", SettingTypes.TYPE_BOOL),
        SettingCollector("local_media_server", SettingTypes.TYPE_STRING),
        SettingCollector("pagan_symbol", SettingTypes.TYPE_STRING),
        SettingCollector("language_ui", SettingTypes.TYPE_STRING),
        SettingCollector("end_list_anim", SettingTypes.TYPE_STRING),
        SettingCollector("runes_show", SettingTypes.TYPE_BOOL),
        SettingCollector("player_background_settings_json", SettingTypes.TYPE_STRING),
        SettingCollector("slidr_settings_json", SettingTypes.TYPE_STRING),
        SettingCollector("use_api_5_90_for_audio", SettingTypes.TYPE_BOOL),
        SettingCollector("is_side_navigation", SettingTypes.TYPE_BOOL),
        SettingCollector("is_side_no_stroke", SettingTypes.TYPE_BOOL),
        SettingCollector("is_side_transition", SettingTypes.TYPE_BOOL),
        SettingCollector("notification_force_link", SettingTypes.TYPE_BOOL),
        SettingCollector("recording_to_opus", SettingTypes.TYPE_BOOL),
        SettingCollector("service_playlists", SettingTypes.TYPE_STRING),
        SettingCollector("rendering_mode", SettingTypes.TYPE_STRING),
        SettingCollector("hidden_peers", SettingTypes.TYPE_STRING_SET),
        SettingCollector("notif_peer_uids", SettingTypes.TYPE_STRING_SET),
        SettingCollector("user_name_changes_uids", SettingTypes.TYPE_STRING_SET)
    )

    fun doBackup(): JsonObject? {
        var has = false
        val pref =
            PreferenceScreen.getPreferences(Injection.provideApplicationContext())
        val ret = JsonObject()
        for (i in settings) {
            val temp = i.requestSetting(pref)
            if (temp != null) {
                if (!has) has = true
                ret.add(i.name, temp)
            }
        }
        for (i in Settings.get().notifications().chatsNotif) {
            val temp = SettingCollector(i.key, SettingTypes.TYPE_INT).requestSetting(pref)
            if (temp != null) {
                if (!has) has = true
                ret.add(i.key, temp)
            }
        }
        for (i in Settings.get().other().userNameChanges) {
            val temp = SettingCollector(i.key, SettingTypes.TYPE_STRING).requestSetting(pref)
            if (temp != null) {
                if (!has) has = true
                ret.add(i.key, temp)
            }
        }
        return if (!has) null else ret
    }

    fun doRestore(ret: JsonObject?) {
        val pref =
            PreferenceScreen.getPreferences(Injection.provideApplicationContext())

        for (i in Settings.get().notifications().chatsNotifKeys) {
            pref.edit().remove(i).apply()
        }

        for (i in Settings.get().other().userNameChangesKeys) {
            pref.edit().remove(i).apply()
        }

        for (i in settings) {
            i.restore(pref, ret)
        }
        Settings.get().security().reloadHiddenDialogSettings()
        Settings.get().notifications().reloadNotifSettings(true)
        for (i in Settings.get().notifications().chatsNotifKeys) {
            SettingCollector(i, SettingTypes.TYPE_INT).restore(pref, ret)
        }
        Settings.get().notifications().reloadNotifSettings(false)

        Settings.get().other().reloadUserNameChangesSettings(true)
        for (i in Settings.get().other().userNameChangesKeys) {
            SettingCollector(i, SettingTypes.TYPE_STRING).restore(pref, ret)
        }
        Settings.get().other().reloadUserNameChangesSettings(false)
    }

    private inner class SettingCollector(
        val name: String,
        @SettingTypes val type: Int
    ) {
        fun restore(pref: SharedPreferences, ret: JsonObject?) {
            try {
                ret ?: return
                pref.edit().remove(name).apply()
                if (!ret.has(name)) return
                val o = ret.getAsJsonObject(name)
                if (o["type"].asInt != type) return
                when (type) {
                    SettingTypes.TYPE_BOOL -> pref.edit().putBoolean(name, o["value"].asBoolean)
                        .apply()
                    SettingTypes.TYPE_INT -> pref.edit().putInt(name, o["value"].asInt).apply()
                    SettingTypes.TYPE_STRING -> pref.edit().putString(name, o["value"].asString)
                        .apply()
                    SettingTypes.TYPE_STRING_SET -> {
                        val arr = o["array"].asJsonArray
                        if (!arr.isEmpty) {
                            val rt = HashSet<String>(arr.size())
                            for (i in arr) {
                                rt.add(i.asString)
                            }
                            pref.edit()
                                .putStringSet(name, rt)
                                .apply()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun requestSetting(pref: SharedPreferences): JsonObject? {
            if (!pref.contains(name)) {
                return null
            }
            val temp = JsonObject()
            temp.addProperty("type", type)
            when (type) {
                SettingTypes.TYPE_BOOL -> temp.addProperty("value", pref.getBoolean(name, false))
                SettingTypes.TYPE_INT -> temp.addProperty("value", pref.getInt(name, 0))
                SettingTypes.TYPE_STRING -> temp.addProperty("value", pref.getString(name, ""))
                SettingTypes.TYPE_STRING_SET -> {
                    val u = JsonArray()
                    val prSet = pref.getStringSet(name, HashSet(0))!!
                    if (prSet.isEmpty()) {
                        return null
                    }
                    for (i in prSet) {
                        u.add(i)
                    }
                    temp.add("array", u)
                }
            }
            return temp
        }
    }
}
