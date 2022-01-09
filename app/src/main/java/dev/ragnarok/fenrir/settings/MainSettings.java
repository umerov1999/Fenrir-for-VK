package dev.ragnarok.fenrir.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.settings.theme.ThemeOverlay;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.view.pager.Transformers_Types;

class MainSettings implements ISettings.IMainSettings {

    private static final String KEY_IMAGE_SIZE = "image_size";

    private final Context app;

    private Optional<Integer> prefferedPhotoPreviewSize;

    MainSettings(Context context) {
        app = context.getApplicationContext();
        prefferedPhotoPreviewSize = Optional.empty();
    }

    @Override
    public boolean isSendByEnter() {
        return getDefaultPreferences().getBoolean("send_by_enter", false);
    }

    @Override
    public @ThemeOverlay
    int getThemeOverlay() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("theme_overlay", "0").trim());
        } catch (Exception e) {
            return ThemeOverlay.OFF;
        }
    }

    @Override
    public boolean isAudio_round_icon() {
        return getDefaultPreferences().getBoolean("audio_round_icon", true);
    }

    @Override
    public boolean isUse_long_click_download() {
        return getDefaultPreferences().getBoolean("use_long_click_download", false);
    }

    @Override
    public boolean isRevert_play_audio() {
        return getDefaultPreferences().getBoolean("revert_play_audio", false);
    }

    @Override
    public boolean isPlayer_support_volume() {
        return getDefaultPreferences().getBoolean("is_player_support_volume", false);
    }

    @Override
    public boolean isShow_bot_keyboard() {
        return getDefaultPreferences().getBoolean("show_bot_keyboard", true);
    }

    @Override
    public boolean isMy_message_no_color() {
        return getDefaultPreferences().getBoolean("my_message_no_color", false);
    }

    @Override
    public boolean isNotification_bubbles_enabled() {
        return getDefaultPreferences().getBoolean("notification_bubbles", true);
    }

    @Override
    public boolean isMessages_menu_down() {
        return getDefaultPreferences().getBoolean("messages_menu_down", false);
    }

    @Override
    public boolean isExpand_voice_transcript() {
        return getDefaultPreferences().getBoolean("expand_voice_transcript", false);
    }

    @Nullable
    @Override
    public Integer getUploadImageSize() {
        String i = getDefaultPreferences().getString(KEY_IMAGE_SIZE, "0");
        switch (i) {
            case "1":
                return Upload.IMAGE_SIZE_800;
            case "2":
                return Upload.IMAGE_SIZE_1200;
            case "3":
                return Upload.IMAGE_SIZE_FULL;
            case "4":
                return Upload.IMAGE_SIZE_CROPPING;
            default:
                return null;
        }
    }

    @Override
    public void setUploadImageSize(Integer size) {
        getDefaultPreferences().edit().putString(KEY_IMAGE_SIZE, String.valueOf(size)).apply();
    }

    @Override
    public int getUploadImageSizePref() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString(KEY_IMAGE_SIZE, "0").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getStart_newsMode() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("start_news", "2").trim());
        } catch (Exception e) {
            return 2;
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public @PhotoSize
    int getPrefPreviewImageSize() {
        if (prefferedPhotoPreviewSize.isEmpty()) {
            prefferedPhotoPreviewSize = Optional.wrap(restorePhotoPreviewSize());
        }

        return prefferedPhotoPreviewSize.get();
    }

    @Override
    public int cryptVersion() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("crypt_version", "1").trim());
        } catch (Exception e) {
            return 1;
        }
    }

    @PhotoSize
    private int restorePhotoPreviewSize() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("photo_preview_size", String.valueOf(PhotoSize.Y)).trim());
        } catch (Exception e) {
            return PhotoSize.Y;
        }
    }

    @Override
    @Transformers_Types
    public int getViewpager_page_transform() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("viewpager_page_transform", String.valueOf(Transformers_Types.OFF)).trim());
        } catch (Exception e) {
            return Transformers_Types.OFF;
        }
    }

    @Override
    @Transformers_Types
    public int getPlayer_cover_transform() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("player_cover_transform", String.valueOf(Transformers_Types.DEPTH_TRANSFORMER)).trim());
        } catch (Exception e) {
            return Transformers_Types.DEPTH_TRANSFORMER;
        }
    }

    private SharedPreferences getDefaultPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Override
    public void notifyPrefPreviewSizeChanged() {
        prefferedPhotoPreviewSize = Optional.empty();
    }

    @PhotoSize
    @Override
    public int getPrefDisplayImageSize(@PhotoSize int byDefault) {
        return getDefaultPreferences().getInt("pref_display_photo_size", byDefault);
    }

    @Override
    public int getPhotoRoundMode() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("photo_rounded_view", "0").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getFontSize() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("font_size", "0").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void setPrefDisplayImageSize(@PhotoSize int size) {
        getDefaultPreferences()
                .edit()
                .putInt("pref_display_photo_size", size)
                .apply();
    }

    @Override
    public int isOpenUrlInternal() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("is_open_url_internal", "1").trim());
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public boolean isWebview_night_mode() {
        return getDefaultPreferences().getBoolean("webview_night_mode", true);
    }

    @Override
    public boolean isLoad_history_notif() {
        return getDefaultPreferences().getBoolean("load_history_notif", false);
    }

    @Override
    public boolean isSnow_mode() {
        return getDefaultPreferences().getBoolean("snow_mode", false);
    }

    @Override
    public boolean isDont_write() {
        return getDefaultPreferences().getBoolean("dont_write", false);
    }

    @Override
    public boolean isOver_ten_attach() {
        return getDefaultPreferences().getBoolean("over_ten_attach", false);
    }
}
