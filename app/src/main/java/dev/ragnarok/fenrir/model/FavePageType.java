package dev.ragnarok.fenrir.model;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({FavePageType.USER, FavePageType.COMMUNITY})
@Retention(RetentionPolicy.SOURCE)
public @interface FavePageType {
    String USER = "user";
    String COMMUNITY = "group";
}
