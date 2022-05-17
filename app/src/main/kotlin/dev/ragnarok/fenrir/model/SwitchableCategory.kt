package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef

@IntDef(
    SwitchableCategory.FRIENDS,
    SwitchableCategory.NEWSFEED_COMMENTS,
    SwitchableCategory.GROUPS,
    SwitchableCategory.PHOTOS,
    SwitchableCategory.VIDEOS,
    SwitchableCategory.MUSIC,
    SwitchableCategory.DOCS,
    SwitchableCategory.BOOKMARKS
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class SwitchableCategory {
    companion object {
        const val FRIENDS = 1
        const val NEWSFEED_COMMENTS = 2
        const val GROUPS = 3
        const val PHOTOS = 4
        const val VIDEOS = 5
        const val MUSIC = 6
        const val DOCS = 7
        const val BOOKMARKS = 8
    }
}