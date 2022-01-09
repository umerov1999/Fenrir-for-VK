package dev.ragnarok.fenrir.model.menu.options;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({VideoLocalServerOption.play_item_video,
        VideoLocalServerOption.save_item_video,
        VideoLocalServerOption.bitrate_item_video,
        VideoLocalServerOption.delete_item_video,
        VideoLocalServerOption.update_time_item_video,
        VideoLocalServerOption.edit_item_video})
@Retention(RetentionPolicy.SOURCE)
public @interface VideoLocalServerOption {
    int play_item_video = 1;
    int save_item_video = 2;
    int bitrate_item_video = 5;
    int delete_item_video = 6;
    int update_time_item_video = 7;
    int edit_item_video = 8;
}

