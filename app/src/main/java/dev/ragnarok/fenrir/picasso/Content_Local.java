package dev.ragnarok.fenrir.picasso;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Content_Local.PHOTO,
        Content_Local.VIDEO,
        Content_Local.AUDIO})
@Retention(RetentionPolicy.SOURCE)
public @interface Content_Local {
    int PHOTO = 1;
    int VIDEO = 2;
    int AUDIO = 3;
}

