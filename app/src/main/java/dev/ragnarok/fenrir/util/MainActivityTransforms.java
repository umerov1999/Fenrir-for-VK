package dev.ragnarok.fenrir.util;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MainActivityTransforms.MAIN,
        MainActivityTransforms.SWIPEBLE,
        MainActivityTransforms.SEND_ATTACHMENTS,
        MainActivityTransforms.PROFILES})
@Retention(RetentionPolicy.SOURCE)
public @interface MainActivityTransforms {
    int MAIN = 1;
    int SWIPEBLE = 2;
    int SEND_ATTACHMENTS = 3;
    int PROFILES = 4;
}