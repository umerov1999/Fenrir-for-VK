package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef

@IntDef(
    SideSwitchableCategory.FRIENDS,
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
    SideSwitchableCategory.NEWSFEED_COMMENTS
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class SideSwitchableCategory {
    companion object {
        const val FRIENDS = 1
        const val DIALOGS = 2
        const val FEED = 3
        const val FEEDBACK = 4
        const val GROUPS = 5
        const val PHOTOS = 6
        const val VIDEOS = 7
        const val MUSIC = 8
        const val DOCS = 9
        const val BOOKMARKS = 10
        const val SEARCH = 11
        const val NEWSFEED_COMMENTS = 12
    }
}