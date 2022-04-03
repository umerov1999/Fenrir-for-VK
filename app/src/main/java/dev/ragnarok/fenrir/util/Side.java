package dev.ragnarok.fenrir.util;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Side.DISABLED, Side.NO_LOADING, Side.LOADING})
@Retention(RetentionPolicy.SOURCE)
public @interface Side {
    int DISABLED = 1;
    int NO_LOADING = 2;
    int LOADING = 3;
}
