package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({ChatAction.NO_ACTION, ChatAction.PHOTO_UPDATE, ChatAction.PHOTO_REMOVE, ChatAction.CREATE,
        ChatAction.TITLE_UPDATE, ChatAction.INVITE_USER, ChatAction.KICK_USER, ChatAction.PIN_MESSAGE, ChatAction.UNPIN_MESSAGE, ChatAction.INVITE_USER_BY_LINK})
@Retention(RetentionPolicy.SOURCE)
public @interface ChatAction {
    int NO_ACTION = 0;
    int PHOTO_UPDATE = 1;
    int PHOTO_REMOVE = 2;
    int CREATE = 3;
    int TITLE_UPDATE = 4;
    int INVITE_USER = 5;
    int KICK_USER = 6;
    int PIN_MESSAGE = 7;
    int UNPIN_MESSAGE = 8;
    int INVITE_USER_BY_LINK = 9;
}
