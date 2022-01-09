package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({Sex.MAN, Sex.WOMAN, Sex.UNKNOWN})
@Retention(RetentionPolicy.SOURCE)
public @interface Sex {
    int MAN = 2;
    int WOMAN = 1;
    int UNKNOWN = 0;
}