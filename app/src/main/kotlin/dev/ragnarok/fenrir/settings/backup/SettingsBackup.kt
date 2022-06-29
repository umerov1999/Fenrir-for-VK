package dev.ragnarok.fenrir.settings.backup

import androidx.annotation.Keep
import de.maxr1998.modernpreferences.PreferenceScreen
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.model.ShortcutStored
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.serializeble.json.JsonObject
import dev.ragnarok.fenrir.util.serializeble.json.JsonObjectBuilder
import dev.ragnarok.fenrir.util.serializeble.json.decodeFromJsonElement
import dev.ragnarok.fenrir.util.serializeble.json.encodeToJsonElement
import dev.ragnarok.fenrir.util.serializeble.prefs.Preferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

class SettingsBackup {
    @Keep
    @Serializable
    @Suppress("unused")
    class AppPreferencesList {
        //Main
        var send_by_enter: Boolean = false
        var theme_overlay: String? = null
        var audio_round_icon: Boolean = false
        var use_long_click_download: Boolean = false
        var revert_play_audio: Boolean = false
        var is_player_support_volume: Boolean = false
        var show_bot_keyboard: Boolean = false
        var my_message_no_color: Boolean = false
        var notification_bubbles: Boolean = false
        var messages_menu_down: Boolean = false
        var expand_voice_transcript: Boolean = false
        var image_size: String? = null
        var start_news: String? = null
        var crypt_version: String? = null
        var photo_preview_size: String? = null
        var viewpager_page_transform: String? = null
        var player_cover_transform: String? = null
        var pref_display_photo_size: Int = 0
        var photo_rounded_view: String? = null
        var font_size: String? = null
        var is_open_url_internal: String? = null
        var webview_night_mode: Boolean = false
        var load_history_notif: Boolean = false
        var snow_mode: Boolean = false
        var dont_write: Boolean = false
        var over_ten_attach: Boolean = false

        //UI
        var avatar_style: Int = 0
        var app_theme: String? = null
        var night_switch: String? = null
        var default_category: String? = null
        var last_closed_place_type: Int = 0
        var emojis_type: Boolean = false
        var emojis_full_screen: Boolean = false
        var stickers_by_theme: Boolean = false
        var stickers_by_new: Boolean = false
        var show_profile_in_additional_page: Boolean = false
        var display_writing: Boolean = false

        //Other
        var swipes_for_chats: String? = null
        var broadcast: Boolean = false
        var comments_desc: Boolean = false
        var keep_longpoll: Boolean = false
        var disable_error_fcm: Boolean = false
        var settings_no_push: Boolean = false
        var videos_ext: Set<String>? = null
        var photo_ext: Set<String>? = null
        var audio_ext: Set<String>? = null
        var enable_dirs_files_count: Boolean = false
        var max_bitmap_resolution: String? = null
        var max_thumb_resolution: String? = null
        var ffmpeg_audio_codecs: String? = null
        var lifecycle_music_service: String? = null
        var autoplay_gif: Boolean = false
        var strip_news_repost: Boolean = false
        var ad_block_story_news: Boolean = false
        var block_news_by_words_set: Set<String>? = null
        var new_loading_dialog: Boolean = false
        var vk_api_domain: String? = null
        var vk_auth_domain: String? = null
        var developer_mode: Boolean = false
        var do_logs: Boolean = false
        var force_cache: Boolean = false
        var disable_history: Boolean = false
        var show_wall_cover: Boolean = false
        var custom_chat_color: Int = 0
        var custom_chat_color_second: Int = 0
        var custom_chat_color_usage: Boolean = false
        var custom_message_color: Int = 0
        var custom_second_message_color: Int = 0
        var custom_message_color_usage: Boolean = false
        var info_reading: Boolean = false
        var auto_read: Boolean = false
        var mark_listened_voice: Boolean = false
        var not_update_dialogs: Boolean = false
        var be_online: Boolean = false
        var donate_anim_set: String? = null
        var use_stop_audio: Boolean = false
        var player_has_background: Boolean = false
        var show_mini_player: Boolean = false
        var enable_last_read: Boolean = false
        var not_read_show: Boolean = false
        var headers_in_dialog: Boolean = false
        var show_recent_dialogs: Boolean = false
        var show_audio_top: Boolean = false
        var use_internal_downloader: Boolean = false
        var music_dir: String? = null
        var photo_dir: String? = null
        var video_dir: String? = null
        var docs_dir: String? = null
        var sticker_dir: String? = null
        var photo_to_user_dir: Boolean = false
        var download_voice_ogg: Boolean = false
        var delete_cache_images: Boolean = false
        var compress_default_traffic: Boolean = false
        var limit_cache: Boolean = false
        var do_not_clear_back_stack: Boolean = false
        var mention_fave: Boolean = false
        var disable_encryption: Boolean = false
        var download_photo_tap: Boolean = false
        var audio_save_mode_button: Boolean = false
        var show_mutual_count: Boolean = false
        var not_friend_show: Boolean = false
        var do_zoom_photo: Boolean = false
        var change_upload_size: Boolean = false
        var show_photos_line: Boolean = false
        var do_auto_play_video: Boolean = false
        var video_controller_to_decor: Boolean = false
        var video_swipes: Boolean = false
        var disable_likes: Boolean = false
        var disable_notifications: Boolean = false
        var native_parcel_photo: Boolean = false
        var native_parcel_story: Boolean = false
        var dump_fcm: Boolean = false
        var hint_stickers: Boolean = false
        var enable_native: Boolean = false
        var enable_cache_ui_anim: Boolean = false
        var disable_sensored_voice: Boolean = false
        var local_media_server: String? = null
        var pagan_symbol: String? = null
        var language_ui: String? = null
        var end_list_anim: String? = null
        var runes_show: Boolean = false
        var player_background_settings_json: String? = null
        var slidr_settings_json: String? = null
        var use_api_5_90_for_audio: Boolean = false
        var is_side_navigation: Boolean = false
        var is_side_no_stroke: Boolean = false
        var is_side_transition: Boolean = false
        var notification_force_link: Boolean = false
        var recording_to_opus: Boolean = false
        var service_playlists: String? = null
        var rendering_mode: String? = null
        var hidden_peers: Set<String>? = null
        var notif_peer_uids: Set<String>? = null
        var user_name_changes_uids: Set<String>? = null
    }

    fun doBackup(): JsonObject {
        val pref = PreferenceScreen.getPreferences(Includes.provideApplicationContext())
        val preferences = Preferences(pref)
        val ret = JsonObjectBuilder()
        ret.put(
            "app",
            kJson.encodeToJsonElement(
                AppPreferencesList.serializer(),
                preferences.decode(AppPreferencesList.serializer(), "")
            )
        )
        val notificatios_pointers = HashMap<String, Int>()
        for ((key) in Settings.get().notifications().chatsNotif) {
            if (pref.contains(key)) {
                notificatios_pointers[key] = pref.getInt(key, -1)
            }
        }
        ret.put("notifications_values", kJson.encodeToJsonElement(notificatios_pointers))
        val user_names_pointers = HashMap<String, String>()
        for ((key) in Settings.get().other().getUserNameChangesMap()) {
            if (pref.contains(key)) {
                user_names_pointers[key] = pref.getString(key, null) ?: continue
            }
        }
        ret.put("user_names_values", kJson.encodeToJsonElement(user_names_pointers))
        val yu = Includes.stores.tempStore().getShortcutAll().blockingGet()
        if (yu.nonNullNoEmpty()) {
            ret.put(
                "shortcuts",
                kJson.encodeToJsonElement(ListSerializer(ShortcutStored.serializer()), yu)
            )
        }
        return ret.build()
    }

    fun doRestore(ret: JsonObject?) {
        ret ?: return
        val pref =
            PreferenceScreen.getPreferences(Includes.provideApplicationContext())
        val preferences = Preferences(pref)

        for (i in Settings.get().notifications().chatsNotifKeys) {
            pref.edit().remove(i).apply()
        }

        for (i in Settings.get().other().userNameChangesKeys) {
            pref.edit().remove(i).apply()
        }

        ret["app"]?.let {
            preferences.encode(
                AppPreferencesList.serializer(),
                "",
                kJson.decodeFromJsonElement(AppPreferencesList.serializer(), it)
            )
        }

        Settings.get().security().reloadHiddenDialogSettings()
        Settings.get().notifications().reloadNotifSettings(true)

        ret["notifications_values"]?.let {
            val notificatios_pointers: HashMap<String, Int> = kJson.decodeFromJsonElement(it)
            for ((key, value) in notificatios_pointers) {
                pref.edit().putInt(key, value).apply()
            }
        }
        Settings.get().notifications().reloadNotifSettings(false)
        Settings.get().other().reloadUserNameChangesSettings(true)

        ret["user_names_values"]?.let {
            val user_names_pointers: HashMap<String, String> = kJson.decodeFromJsonElement(it)
            for ((key, value) in user_names_pointers) {
                pref.edit().putString(key, value).apply()
            }
        }
        Settings.get().other().reloadUserNameChangesSettings(false)

        ret["shortcuts"]?.let {
            val jp = kJson.decodeFromJsonElement(ListSerializer(ShortcutStored.serializer()), it)
            if (jp.nonNullNoEmpty()) {
                Includes.stores.tempStore().addShortcuts(jp.reversed()).blockingAwait()
            }
        }
    }
}
