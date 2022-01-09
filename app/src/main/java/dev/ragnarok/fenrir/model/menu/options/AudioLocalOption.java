package dev.ragnarok.fenrir.model.menu.options;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AudioLocalOption.play_item_audio,
        AudioLocalOption.upload_item_audio,
        AudioLocalOption.play_item_after_current_audio,
        AudioLocalOption.play_via_local_server,
        AudioLocalOption.bitrate_item_audio,
        AudioLocalOption.delete_item_audio})
@Retention(RetentionPolicy.SOURCE)
public @interface AudioLocalOption {
    int play_item_audio = 1;
    int upload_item_audio = 2;
    int play_item_after_current_audio = 3;
    int play_via_local_server = 4;
    int bitrate_item_audio = 5;
    int delete_item_audio = 6;
}

