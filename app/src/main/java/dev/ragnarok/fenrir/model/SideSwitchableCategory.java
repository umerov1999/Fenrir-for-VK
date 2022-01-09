package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({SideSwitchableCategory.FRIENDS,
        SideSwitchableCategory.DIALOGS,
        SideSwitchableCategory.FEED,
        SideSwitchableCategory.FEEDBACK,
        SideSwitchableCategory.GROUPS,
        SideSwitchableCategory.PHOTOS,
        SideSwitchableCategory.VIDEOS,
        SideSwitchableCategory.MUSIC,
        SideSwitchableCategory.DOCS,
        SideSwitchableCategory.BOOKMARKS,
        SideSwitchableCategory.SEARCH,
        SideSwitchableCategory.NEWSFEED_COMMENTS})
@Retention(RetentionPolicy.SOURCE)
public @interface SideSwitchableCategory {
    int FRIENDS = 1;
    int DIALOGS = 2;
    int FEED = 3;
    int FEEDBACK = 4;
    int GROUPS = 5;
    int PHOTOS = 6;
    int VIDEOS = 7;
    int MUSIC = 8;
    int DOCS = 9;
    int BOOKMARKS = 10;
    int SEARCH = 11;
    int NEWSFEED_COMMENTS = 12;
}
