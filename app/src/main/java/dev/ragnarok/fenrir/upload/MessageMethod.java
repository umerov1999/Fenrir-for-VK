package dev.ragnarok.fenrir.upload;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MessageMethod.NULL,
        MessageMethod.PHOTO,
        MessageMethod.VIDEO})
@Retention(RetentionPolicy.SOURCE)
public @interface MessageMethod {
    int NULL = 1;
    int PHOTO = 2;
    int VIDEO = 3;
}