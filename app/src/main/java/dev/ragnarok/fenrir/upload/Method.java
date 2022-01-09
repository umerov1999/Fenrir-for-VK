package dev.ragnarok.fenrir.upload;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Method.PHOTO_TO_ALBUM,
        Method.TO_WALL,
        Method.TO_COMMENT,
        Method.PHOTO_TO_PROFILE,
        Method.PHOTO_TO_CHAT,
        Method.TO_MESSAGE,
        Method.AUDIO,
        Method.VIDEO,
        Method.DOCUMENT,
        Method.STORY,
        Method.REMOTE_PLAY_AUDIO})
@Retention(RetentionPolicy.SOURCE)
public @interface Method {
    int PHOTO_TO_ALBUM = 1;
    int TO_WALL = 2;
    int TO_COMMENT = 3;
    int PHOTO_TO_PROFILE = 4;
    int PHOTO_TO_CHAT = 5;
    int TO_MESSAGE = 6;
    int AUDIO = 7;
    int VIDEO = 8;
    int DOCUMENT = 9;
    int STORY = 10;
    int REMOTE_PLAY_AUDIO = 11;
}

