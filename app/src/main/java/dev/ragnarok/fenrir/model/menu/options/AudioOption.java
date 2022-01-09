package dev.ragnarok.fenrir.model.menu.options;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AudioOption.play_item_audio,
        AudioOption.play_item_after_current_audio,
        AudioOption.add_item_audio,
        AudioOption.save_item_audio,
        AudioOption.get_recommendation_by_audio,
        AudioOption.open_album,
        AudioOption.get_lyrics_menu,
        AudioOption.copy_url,
        AudioOption.bitrate_item_audio,
        AudioOption.search_by_artist,
        AudioOption.share_button,
        AudioOption.add_and_download_button,
        AudioOption.goto_artist,
        AudioOption.edit_track})
@Retention(RetentionPolicy.SOURCE)
public @interface AudioOption {
    int play_item_audio = 1;
    int play_item_after_current_audio = 2;
    int add_item_audio = 3;
    int save_item_audio = 4;
    int get_recommendation_by_audio = 5;
    int open_album = 6;
    int get_lyrics_menu = 7;
    int copy_url = 8;
    int bitrate_item_audio = 9;
    int search_by_artist = 10;
    int share_button = 11;
    int add_and_download_button = 12;
    int goto_artist = 13;
    int edit_track = 14;
}

