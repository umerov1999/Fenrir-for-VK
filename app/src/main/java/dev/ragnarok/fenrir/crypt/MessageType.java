package dev.ragnarok.fenrir.crypt;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MessageType.KEY_EXCHANGE, MessageType.CRYPTED, MessageType.NORMAL})
@Retention(RetentionPolicy.SOURCE)
public @interface MessageType {
    int KEY_EXCHANGE = 1;
    int CRYPTED = 2;
    int NORMAL = 0;
}
