package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MessageFlag.UNREAD, MessageFlag.OUTBOX, MessageFlag.REPLIED, MessageFlag.IMPORTANT,
        MessageFlag.DIALOG, MessageFlag.FRIENDS, MessageFlag.SPAM, MessageFlag.DELETED,
        MessageFlag.FIXED, MessageFlag.MEDIA, MessageFlag.GROUP_CHAT})
@Retention(RetentionPolicy.SOURCE)
public @interface MessageFlag {
    int UNREAD = 1; //сообщение не прочитано
    int OUTBOX = 2; //исходящее сообщение
    int REPLIED = 4; //на сообщение был создан ответ
    int IMPORTANT = 8; //помеченное сообщение
    int DIALOG = 16; //сообщение отправлено через диалог
    int FRIENDS = 32; //сообщение отправлено другом
    int SPAM = 64; //сообщение помечено как "Спам"
    int DELETED = 128; //сообщение удалено (в корзине)
    int FIXED = 256; //сообщение проверено пользователем на спам
    int MEDIA = 512; //сообщение содержит медиаконтент
    int GROUP_CHAT = 8192;    //беседа
}
