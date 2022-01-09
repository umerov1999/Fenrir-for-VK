package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MessageType.NO, MessageType.STICKER, MessageType.GRAFFITY, MessageType.CALL, MessageType.GIFT,
        MessageType.VOICE, MessageType.VIDEO, MessageType.AUDIO, MessageType.DOC, MessageType.PHOTO, MessageType.WALL, MessageType.OTHERS})
@Retention(RetentionPolicy.SOURCE)
public @interface MessageType {
    int NO = 0;
    int STICKER = 1;
    int GRAFFITY = 2;
    int CALL = 3;
    int GIFT = 4;
    int VOICE = 5;
    int VIDEO = 6;
    int AUDIO = 7;
    int DOC = 8;
    int PHOTO = 9;
    int WALL = 10;
    int OTHERS = 11;
}
