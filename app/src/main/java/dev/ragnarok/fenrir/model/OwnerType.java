package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({OwnerType.USER, OwnerType.COMMUNITY})
@Retention(RetentionPolicy.SOURCE)
public @interface OwnerType {
    int USER = 1;
    int COMMUNITY = 2;
}
