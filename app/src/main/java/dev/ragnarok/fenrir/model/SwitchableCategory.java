package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({SwitchableCategory.FRIENDS,
        SwitchableCategory.NEWSFEED_COMMENTS,
        SwitchableCategory.GROUPS,
        SwitchableCategory.PHOTOS,
        SwitchableCategory.VIDEOS,
        SwitchableCategory.MUSIC,
        SwitchableCategory.DOCS,
        SwitchableCategory.BOOKMARKS})
@Retention(RetentionPolicy.SOURCE)
public @interface SwitchableCategory {
    int FRIENDS = 1;
    int NEWSFEED_COMMENTS = 2;
    int GROUPS = 3;
    int PHOTOS = 4;
    int VIDEOS = 5;
    int MUSIC = 6;
    int DOCS = 7;
    int BOOKMARKS = 8;
}
