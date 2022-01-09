package dev.ragnarok.fenrir.settings;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({SwipesChatMode.DISABLED, SwipesChatMode.SLIDR})
@Retention(RetentionPolicy.SOURCE)
public @interface SwipesChatMode {
    int DISABLED = 0;
    int SLIDR = 1;
}
