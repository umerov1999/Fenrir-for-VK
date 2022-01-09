package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({EditingPostType.DRAFT, EditingPostType.TEMP})
@Retention(RetentionPolicy.SOURCE)
public @interface EditingPostType {
    int DRAFT = 2;
    int TEMP = 3;
}
