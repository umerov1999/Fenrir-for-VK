package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MessageStatus.SENT, MessageStatus.SENDING, MessageStatus.QUEUE, MessageStatus.ERROR,
        MessageStatus.EDITING, MessageStatus.WAITING_FOR_UPLOAD})
@Retention(RetentionPolicy.SOURCE)
public @interface MessageStatus {
    int SENT = 1;
    int SENDING = 2;
    int QUEUE = 3;
    int ERROR = 4;
    int EDITING = 6;
    int WAITING_FOR_UPLOAD = 7;
}
