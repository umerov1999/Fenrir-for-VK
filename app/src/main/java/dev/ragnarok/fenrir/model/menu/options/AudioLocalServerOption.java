package dev.ragnarok.fenrir.model.menu.options;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AudioLocalServerOption.play_item_audio,
        AudioLocalServerOption.save_item_audio,
        AudioLocalServerOption.play_item_after_current_audio,
        AudioLocalServerOption.play_via_local_server,
        AudioLocalServerOption.bitrate_item_audio,
        AudioLocalServerOption.delete_item_audio,
        AudioLocalServerOption.update_time_item_audio,
        AudioLocalServerOption.edit_item_audio})
@Retention(RetentionPolicy.SOURCE)
public @interface AudioLocalServerOption {
    int play_item_audio = 1;
    int save_item_audio = 2;
    int play_item_after_current_audio = 3;
    int play_via_local_server = 4;
    int bitrate_item_audio = 5;
    int delete_item_audio = 6;
    int update_time_item_audio = 7;
    int edit_item_audio = 8;
}

