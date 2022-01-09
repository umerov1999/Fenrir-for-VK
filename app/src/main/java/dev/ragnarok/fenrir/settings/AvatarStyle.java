package dev.ragnarok.fenrir.settings;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AvatarStyle.CIRCLE, AvatarStyle.OVAL})
@Retention(RetentionPolicy.SOURCE)
public @interface AvatarStyle {
    int CIRCLE = 1;
    int OVAL = 2;
}
