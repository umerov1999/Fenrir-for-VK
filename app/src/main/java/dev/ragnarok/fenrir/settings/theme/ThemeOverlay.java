package dev.ragnarok.fenrir.settings.theme;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ThemeOverlay.OFF,
        ThemeOverlay.AMOLED,
        ThemeOverlay.MD1})
@Retention(RetentionPolicy.SOURCE)
public @interface ThemeOverlay {
    int OFF = 0;
    int AMOLED = 1;
    int MD1 = 2;
}

