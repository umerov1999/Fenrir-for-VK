package dev.ragnarok.fenrir.settings;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.maxr1998.modernpreferences.PreferenceScreen;
import dev.ragnarok.fenrir.BuildConfig;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.model.LocalServerSettings;
import dev.ragnarok.fenrir.api.model.PlayerCoverBackgroundSettings;
import dev.ragnarok.fenrir.api.model.SlidrSettings;
import dev.ragnarok.fenrir.model.Lang;
import dev.ragnarok.fenrir.util.Utils;

class OtherSettings implements ISettings.IOtherSettings {

    private static final String KEY_JSON_STATE = "json_list_state";
    private static final String KEY_USERNAME_UIDS = "user_name_changes_uids";

    private final Context app;
    private final Set<String> userNameChanges;
    private final Map<String, String> types;

    OtherSettings(Context context) {
        app = context.getApplicationContext();

        userNameChanges = Collections.synchronizedSet(new HashSet<>(1));
        types = Collections.synchronizedMap(new HashMap<>(1));
        reloadUserNameChangesSettings(false);
    }

    private static String keyForUserNameChanges(int userId) {
        return "custom_user_name_" + userId;
    }

    @NonNull
    @Override
    public Map<String, String> getUserNameChanges() {
        return new HashMap<>(types);
    }

    @NonNull
    @Override
    public Set<String> getUserNameChangesKeys() {
        return new HashSet<>(userNameChanges);
    }

    @Override
    public void reloadUserNameChangesSettings(boolean onlyRoot) {
        SharedPreferences preferences = PreferenceScreen.getPreferences(app);
        userNameChanges.clear();
        userNameChanges.addAll(preferences.getStringSet(KEY_USERNAME_UIDS, new HashSet<>(1)));
        if (onlyRoot) {
            return;
        }
        types.clear();
        for (String i : userNameChanges) {
            String rs = preferences.getString(i, null);
            if (!Utils.isEmpty(rs)) {
                types.put(i, rs);
            }
        }
    }

    @Override
    public void setUserNameChanges(int userId, @Nullable String name) {
        SharedPreferences preferences = PreferenceScreen.getPreferences(app);
        if (Utils.isEmpty(name)) {
            userNameChanges.remove(keyForUserNameChanges(userId));
            types.remove(keyForUserNameChanges(userId));
            preferences.edit()
                    .remove(keyForUserNameChanges(userId))
                    .putStringSet(KEY_USERNAME_UIDS, userNameChanges)
                    .apply();
        } else {
            userNameChanges.add(keyForUserNameChanges(userId));
            types.put(keyForUserNameChanges(userId), name);
            preferences.edit()
                    .putString(keyForUserNameChanges(userId), name)
                    .putStringSet(KEY_USERNAME_UIDS, userNameChanges)
                    .apply();
        }
    }

    @Override
    public @Nullable
    String getUserNameChanges(int userId) {
        if (types.containsKey(keyForUserNameChanges(userId))) {
            String v = types.get(keyForUserNameChanges(userId));
            if (!Utils.isEmpty(v)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public String getFeedSourceIds(int accountId) {
        return PreferenceScreen.getPreferences(app)
                .getString("source_ids" + accountId, null);
    }

    @Override
    public void setFeedSourceIds(int accountId, String sourceIds) {
        PreferenceScreen.getPreferences(app)
                .edit()
                .putString("source_ids" + accountId, sourceIds)
                .apply();
    }

    @Override
    public void storeFeedScrollState(int accountId, String state) {
        if (nonNull(state)) {
            PreferenceScreen.getPreferences(app)
                    .edit()
                    .putString(KEY_JSON_STATE + accountId, state)
                    .apply();
        } else {
            PreferenceScreen.getPreferences(app)
                    .edit()
                    .remove(KEY_JSON_STATE + accountId)
                    .apply();
        }
    }

    @Override
    public String restoreFeedScrollState(int accountId) {
        return PreferenceScreen.getPreferences(app).getString(KEY_JSON_STATE + accountId, null);
    }

    @Override
    public String restoreFeedNextFrom(int accountId) {
        return PreferenceScreen.getPreferences(app)
                .getString("next_from" + accountId, null);
    }

    @Override
    public void storeFeedNextFrom(int accountId, String nextFrom) {
        PreferenceScreen.getPreferences(app)
                .edit()
                .putString("next_from" + accountId, nextFrom)
                .apply();
    }

    @Override
    public boolean isAudioBroadcastActive() {
        return PreferenceScreen.getPreferences(app).getBoolean("broadcast", false);
    }

    @Override
    public boolean isCommentsDesc() {
        return PreferenceScreen.getPreferences(app).getBoolean("comments_desc", true);
    }

    @Override
    public boolean toggleCommentsDirection() {
        boolean descNow = isCommentsDesc();

        PreferenceScreen.getPreferences(app)
                .edit()
                .putBoolean("comments_desc", !descNow)
                .apply();

        return !descNow;
    }

    @Override
    public boolean isKeepLongpoll() {
        return PreferenceScreen.getPreferences(app).getBoolean("keep_longpoll", false);
    }

    @Override
    public void setDisableErrorFCM(boolean en) {
        PreferenceScreen.getPreferences(app).edit().putBoolean("disable_error_fcm", en).apply();
    }

    @Override
    public boolean isDisabledErrorFCM() {
        return PreferenceScreen.getPreferences(app).getBoolean("disable_error_fcm", false);
    }

    @Override
    public boolean isSettings_no_push() {
        return PreferenceScreen.getPreferences(app).getBoolean("settings_no_push", false);
    }

    @Override
    public int getMaxBitmapResolution() {
        try {
            return Integer.parseInt(PreferenceScreen.getPreferences(app).getString("max_bitmap_resolution", "4000").trim());
        } catch (Exception e) {
            return 4000;
        }
    }

    @Override
    public @NonNull
    List<Integer> getServicePlaylist() {
        try {
            String rs = PreferenceScreen.getPreferences(app).getString("service_playlists", "-21 -22 -25 -26 -27 -28").trim();
            if (Utils.isEmpty(rs)) {
                return Collections.emptyList();
            }
            String[] integerStrings = rs.split(" ");
            if (integerStrings.length <= 0) {
                return Collections.emptyList();
            }
            List<Integer> integers = new ArrayList<>(integerStrings.length);
            for (int i = 0; i < integerStrings.length; i++) {
                integers.add(i, Integer.parseInt(integerStrings[i].trim()));
            }
            return integers;
        } catch (Exception e) {
            return Arrays.asList(-21, -22, -25, -26, -27, -28);
        }
    }

    @Override
    public int getFFmpegPlugin() {
        try {
            return Integer.parseInt(PreferenceScreen.getPreferences(app).getString("ffmpeg_audio_codecs", "1").trim());
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public int getMusicLifecycle() {
        try {
            int v = Integer.parseInt(PreferenceScreen.getPreferences(app).getString("lifecycle_music_service", String.valueOf(Constants.AUDIO_PLAYER_SERVICE_IDLE)).trim());
            if (v < 60000) {
                PreferenceScreen.getPreferences(app).edit().putString("lifecycle_music_service", "60000").apply();
                v = 60000;
            }
            return v;
        } catch (Exception e) {
            return Constants.AUDIO_PLAYER_SERVICE_IDLE;
        }
    }

    @Override
    public boolean isAutoplay_gif() {
        return PreferenceScreen.getPreferences(app).getBoolean("autoplay_gif", true);
    }

    @Override
    public boolean isStrip_news_repost() {
        return PreferenceScreen.getPreferences(app).getBoolean("strip_news_repost", false);
    }

    @Override
    public boolean isAd_block_story_news() {
        return PreferenceScreen.getPreferences(app).getBoolean("ad_block_story_news", true);
    }

    @Override
    public Set<String> isBlock_news_by_words() {
        return PreferenceScreen.getPreferences(app).getStringSet("block_news_by_words_set", null);
    }

    @Override
    public boolean isNew_loading_dialog() {
        return PreferenceScreen.getPreferences(app).getBoolean("new_loading_dialog", true);
    }

    @Override
    public String get_Api_Domain() {
        return PreferenceScreen.getPreferences(app).getString("vk_api_domain", "api.vk.com").trim();
    }

    @Override
    public String get_Auth_Domain() {
        return PreferenceScreen.getPreferences(app).getString("vk_auth_domain", "oauth.vk.com").trim();
    }

    @Override
    public boolean isDeveloper_mode() {
        return PreferenceScreen.getPreferences(app).getBoolean("developer_mode", true);
    }

    @Override
    public boolean isForce_cache() {
        return PreferenceScreen.getPreferences(app).getBoolean("force_cache", false);
    }

    @Override
    public boolean isUse_api_5_90_for_audio() {
        return PreferenceScreen.getPreferences(app).getBoolean("use_api_5_90_for_audio", true);
    }

    @Override
    public boolean isDisable_history() {
        return PreferenceScreen.getPreferences(app).getBoolean("disable_history", false);
    }

    @Override
    public boolean isShow_wall_cover() {
        return PreferenceScreen.getPreferences(app).getBoolean("show_wall_cover", true);
    }

    @Override
    public int getColorChat() {
        return PreferenceScreen.getPreferences(app).getInt("custom_chat_color", Color.argb(255, 255, 255, 255));
    }

    @Override
    public int getSecondColorChat() {
        return PreferenceScreen.getPreferences(app).getInt("custom_chat_color_second", Color.argb(255, 255, 255, 255));
    }

    @Override
    public boolean isCustom_chat_color() {
        return PreferenceScreen.getPreferences(app).getBoolean("custom_chat_color_usage", false);
    }

    @Override
    public int getColorMyMessage() {
        return PreferenceScreen.getPreferences(app).getInt("custom_message_color", Color.parseColor("#CBD438FF"));
    }

    @Override
    public int getSecondColorMyMessage() {
        return PreferenceScreen.getPreferences(app).getInt("custom_second_message_color", Color.parseColor("#BF6539DF"));
    }

    @Override
    public boolean isCustom_MyMessage() {
        return PreferenceScreen.getPreferences(app).getBoolean("custom_message_color_usage", false);
    }

    @Override
    public boolean isInfo_reading() {
        return PreferenceScreen.getPreferences(app).getBoolean("info_reading", true);
    }

    @Override
    public boolean isAuto_read() {
        return PreferenceScreen.getPreferences(app).getBoolean("auto_read", false);
    }

    @Override
    public boolean isNot_update_dialogs() {
        return PreferenceScreen.getPreferences(app).getBoolean("not_update_dialogs", false);
    }

    @Override
    public boolean isBe_online() {
        return PreferenceScreen.getPreferences(app).getBoolean("be_online", false);
    }

    @Override
    public int getDonate_anim_set() {
        try {
            return Integer.parseInt(PreferenceScreen.getPreferences(app).getString("donate_anim_set", "2").trim());
        } catch (Exception e) {
            return 2;
        }
    }

    @Override
    public boolean isUse_stop_audio() {
        return PreferenceScreen.getPreferences(app).getBoolean("use_stop_audio", false);
    }

    @Override
    public boolean isPlayer_Has_Background() {
        return PreferenceScreen.getPreferences(app).getBoolean("player_has_background", true);
    }

    @Override
    public boolean isShow_mini_player() {
        return PreferenceScreen.getPreferences(app).getBoolean("show_mini_player", true);
    }

    @Override
    public boolean isEnable_last_read() {
        return PreferenceScreen.getPreferences(app).getBoolean("enable_last_read", false);
    }

    @Override
    public boolean isNot_read_show() {
        return PreferenceScreen.getPreferences(app).getBoolean("not_read_show", true);
    }

    @Override
    public boolean isHeaders_in_dialog() {
        return PreferenceScreen.getPreferences(app).getBoolean("headers_in_dialog", true);
    }

    @Override
    public boolean isEnable_show_recent_dialogs() {
        return PreferenceScreen.getPreferences(app).getBoolean("show_recent_dialogs", true);
    }

    @Override
    public boolean is_side_navigation() {
        return PreferenceScreen.getPreferences(app).getBoolean("is_side_navigation", false);
    }

    @Override
    public boolean is_side_no_stroke() {
        return PreferenceScreen.getPreferences(app).getBoolean("is_side_no_stroke", false);
    }

    @Override
    public boolean is_side_transition() {
        return PreferenceScreen.getPreferences(app).getBoolean("is_side_transition", true);
    }

    @Override
    public boolean is_notification_force_link() {
        return PreferenceScreen.getPreferences(app).getBoolean("notification_force_link", false);
    }

    @Override
    public boolean isEnable_show_audio_top() {
        return PreferenceScreen.getPreferences(app).getBoolean("show_audio_top", false);
    }

    @Override
    public boolean isUse_internal_downloader() {
        return PreferenceScreen.getPreferences(app).getBoolean("use_internal_downloader", true);
    }

    @Override
    public boolean appStoredVersionEqual() {
        boolean ret = PreferenceScreen.getPreferences(app).getInt("app_stored_version", 0) == BuildConfig.VERSION_CODE;
        if (!ret) {
            PreferenceScreen.getPreferences(app).edit().putInt("app_stored_version", BuildConfig.VERSION_CODE).apply();
        }
        return ret;
    }

    @Override
    public String getMusicDir() {
        String ret = PreferenceScreen.getPreferences(app).getString("music_dir", null);
        if (Utils.isEmpty(ret) || !new File(ret).exists()) {
            ret = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
            PreferenceScreen.getPreferences(app).edit().putString("music_dir", ret).apply();
        }
        return ret;
    }

    @Override
    public String getPhotoDir() {
        String ret = PreferenceScreen.getPreferences(app).getString("photo_dir", null);
        if (Utils.isEmpty(ret) || !new File(ret).exists()) {
            ret = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Fenrir";
            PreferenceScreen.getPreferences(app).edit().putString("photo_dir", ret).apply();
        }
        return ret;
    }

    @Override
    public String getVideoDir() {
        String ret = PreferenceScreen.getPreferences(app).getString("video_dir", null);
        if (Utils.isEmpty(ret) || !new File(ret).exists()) {
            ret = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/Fenrir";
            PreferenceScreen.getPreferences(app).edit().putString("video_dir", ret).apply();
        }
        return ret;
    }

    @Override
    public String getDocDir() {
        String ret = PreferenceScreen.getPreferences(app).getString("docs_dir", null);
        if (Utils.isEmpty(ret) || !new File(ret).exists()) {
            ret = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Fenrir";
            PreferenceScreen.getPreferences(app).edit().putString("docs_dir", ret).apply();
        }
        return ret;
    }

    @Override
    public String getStickerDir() {
        String ret = PreferenceScreen.getPreferences(app).getString("sticker_dir", null);
        if (Utils.isEmpty(ret) || !new File(ret).exists()) {
            ret = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Fenrir_Stickers";
            PreferenceScreen.getPreferences(app).edit().putString("sticker_dir", ret).apply();
        }
        return ret;
    }

    @Override
    public boolean isPhoto_to_user_dir() {
        return PreferenceScreen.getPreferences(app).getBoolean("photo_to_user_dir", true);
    }

    @Override
    public boolean isDownload_voice_ogg() {
        return PreferenceScreen.getPreferences(app).getBoolean("download_voice_ogg", true);
    }

    @Override
    public boolean isDelete_cache_images() {
        return PreferenceScreen.getPreferences(app).getBoolean("delete_cache_images", false);
    }

    @Override
    public boolean isCompress_traffic() {
        return PreferenceScreen.getPreferences(app).getBoolean("compress_traffic", false);
    }

    @Override
    public boolean isDo_not_clear_back_stack() {
        return PreferenceScreen.getPreferences(app).getBoolean("do_not_clear_back_stack", false);
    }

    @Override
    public boolean isMention_fave() {
        return PreferenceScreen.getPreferences(app).getBoolean("mention_fave", false);
    }

    @Override
    public boolean isDisabled_encryption() {
        return PreferenceScreen.getPreferences(app).getBoolean("disable_encryption", false);
    }

    @Override
    public boolean isDownload_photo_tap() {
        return PreferenceScreen.getPreferences(app).getBoolean("download_photo_tap", true);
    }

    @Override
    public boolean isAudio_save_mode_button() {
        return PreferenceScreen.getPreferences(app).getBoolean("audio_save_mode_button", true);
    }

    @Override
    public boolean isShow_mutual_count() {
        return PreferenceScreen.getPreferences(app).getBoolean("show_mutual_count", false);
    }

    @Override
    public boolean isNot_friend_show() {
        return PreferenceScreen.getPreferences(app).getBoolean("not_friend_show", false);
    }

    @Override
    public boolean isDo_zoom_photo() {
        return PreferenceScreen.getPreferences(app).getBoolean("do_zoom_photo", true);
    }

    @Override
    public boolean isChange_upload_size() {
        return PreferenceScreen.getPreferences(app).getBoolean("change_upload_size", false);
    }

    @Override
    public boolean isShow_photos_line() {
        return PreferenceScreen.getPreferences(app).getBoolean("show_photos_line", true);
    }

    @Override
    public boolean isDo_auto_play_video() {
        return PreferenceScreen.getPreferences(app).getBoolean("do_auto_play_video", false);
    }

    @Override
    public boolean isVideo_controller_to_decor() {
        return PreferenceScreen.getPreferences(app).getBoolean("video_controller_to_decor", false);
    }

    @Override
    public boolean isVideo_swipes() {
        return PreferenceScreen.getPreferences(app).getBoolean("video_swipes", true);
    }

    @Override
    public boolean isDisable_likes() {
        return PreferenceScreen.getPreferences(app).getBoolean("disable_likes", false);
    }

    @Override
    public void setDisable_likes(boolean disabled) {
        PreferenceScreen.getPreferences(app).edit().putBoolean("disable_likes", disabled).apply();
    }

    @Override
    public boolean isDisable_notifications() {
        return PreferenceScreen.getPreferences(app).getBoolean("disable_notifications", false);
    }

    @Override
    public void setDisable_notifications(boolean disabled) {
        PreferenceScreen.getPreferences(app).edit().putBoolean("disable_notifications", disabled).apply();
    }

    @Override
    public boolean isNative_parcel_photo() {
        return PreferenceScreen.getPreferences(app).getBoolean("native_parcel_photo", true);
    }

    @Override
    public boolean isNative_parcel_story() {
        return PreferenceScreen.getPreferences(app).getBoolean("native_parcel_story", true);
    }

    @Override
    public boolean isDoLogs() {
        return isDeveloper_mode() && PreferenceScreen.getPreferences(app).getBoolean("do_logs", false);
    }

    @Override
    public boolean isExtra_debug() {
        return isDoLogs() && PreferenceScreen.getPreferences(app).getBoolean("extra_debug", false);
    }

    @Override
    public boolean isDump_fcm() {
        return isDoLogs() && PreferenceScreen.getPreferences(app).getBoolean("dump_fcm", false);
    }

    @Override
    public boolean isHint_stickers() {
        return PreferenceScreen.getPreferences(app).getBoolean("hint_stickers", true);
    }

    @Override
    public boolean isEnable_native() {
        return PreferenceScreen.getPreferences(app).getBoolean("enable_native", true);
    }

    @Override
    public boolean isEnable_cache_ui_anim() {
        return PreferenceScreen.getPreferences(app).getBoolean("enable_cache_ui_anim", false);
    }

    @Override
    public boolean isRecording_to_opus() {
        return PreferenceScreen.getPreferences(app).getBoolean("recording_to_opus", false);
    }

    @Override
    public boolean isDisable_sensored_voice() {
        return PreferenceScreen.getPreferences(app).getBoolean("disable_sensored_voice", false);
    }

    @Override
    public boolean isInvertPhotoRev() {
        return PreferenceScreen.getPreferences(app).getBoolean("invert_photo_rev", false);
    }

    @Override
    public void setInvertPhotoRev(boolean rev) {
        PreferenceScreen.getPreferences(app).edit().putBoolean("invert_photo_rev", rev).apply();
    }

    @Override
    public @NonNull
    LocalServerSettings getLocalServer() {
        String ret = PreferenceScreen.getPreferences(app).getString("local_media_server", null);
        if (ret == null) {
            return new LocalServerSettings();
        } else {
            return new Gson().fromJson(ret, LocalServerSettings.class);
        }
    }

    @Override
    public void setLocalServer(@NonNull LocalServerSettings settings) {
        PreferenceScreen.getPreferences(app).edit().putString("local_media_server", new Gson().toJson(settings)).apply();
    }

    @Override
    public @NonNull
    PlayerCoverBackgroundSettings getPlayerCoverBackgroundSettings() {
        String ret = PreferenceScreen.getPreferences(app).getString("player_background_settings_json", null);
        if (ret == null) {
            return new PlayerCoverBackgroundSettings().set_default();
        } else {
            return new Gson().fromJson(ret, PlayerCoverBackgroundSettings.class);
        }
    }

    @Override
    public void setPlayerCoverBackgroundSettings(@NonNull PlayerCoverBackgroundSettings settings) {
        PreferenceScreen.getPreferences(app).edit().putString("player_background_settings_json", new Gson().toJson(settings)).apply();
    }

    @Override
    public @NonNull
    SlidrSettings getSlidrSettings() {
        String ret = PreferenceScreen.getPreferences(app).getString("slidr_settings_json", null);
        if (ret == null) {
            return new SlidrSettings().set_default();
        } else {
            return new Gson().fromJson(ret, SlidrSettings.class);
        }
    }

    @Override
    public void setSlidrSettings(@NonNull SlidrSettings settings) {
        PreferenceScreen.getPreferences(app).edit().putString("slidr_settings_json", new Gson().toJson(settings)).apply();
    }

    @Lang
    @Override
    public int getLanguage() {
        try {
            return Integer.parseInt(PreferenceScreen.getPreferences(app).getString("language_ui", "0").trim());
        } catch (Exception e) {
            return Lang.DEFAULT;
        }
    }

    @Override
    public int getRendering_mode() {
        try {
            return Integer.parseInt(PreferenceScreen.getPreferences(app).getString("rendering_mode", "0").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getEndListAnimation() {
        try {
            return Integer.parseInt(PreferenceScreen.getPreferences(app).getString("end_list_anim", "0").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean isRunes_show() {
        return PreferenceScreen.getPreferences(app).getBoolean("runes_show", true);
    }

    @Override
    public int getPaganSymbol() {
        try {
            return Integer.parseInt(PreferenceScreen.getPreferences(app).getString("pagan_symbol", "1").trim());
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public int getCustomChannelNotif() {
        return PreferenceScreen.getPreferences(app).getInt("custom_notification_channel", 0);
    }

    @Override
    public void nextCustomChannelNotif() {
        int vl = getCustomChannelNotif();
        PreferenceScreen.getPreferences(app).edit().putInt("custom_notification_channel", vl + 1).apply();
    }
}
