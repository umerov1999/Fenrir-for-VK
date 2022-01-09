package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({Lang.DEFAULT, Lang.ENGLISH, Lang.RUSSIA})
@Retention(RetentionPolicy.SOURCE)
public @interface Lang {
    int DEFAULT = 0;
    int ENGLISH = 1;
    int RUSSIA = 2;
}
