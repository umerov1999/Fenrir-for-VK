package dev.ragnarok.fenrir.db;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({AttachToType.MESSAGE, AttachToType.COMMENT, AttachToType.POST})
@Retention(RetentionPolicy.SOURCE)
public @interface AttachToType {
    int MESSAGE = 1;
    int COMMENT = 2;
    int POST = 3;
}
