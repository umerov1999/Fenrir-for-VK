package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({CommentedType.POST, CommentedType.PHOTO, CommentedType.VIDEO, CommentedType.TOPIC})
@Retention(RetentionPolicy.SOURCE)
public @interface CommentedType {
    int POST = 1;
    int PHOTO = 2;
    int VIDEO = 3;
    int TOPIC = 4;
}
