package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef

@IntDef(
    NavigationPage.OTHER,
    NavigationPage.FRIENDS,
    NavigationPage.DIALOGS,
    NavigationPage.FEED,
    NavigationPage.MUSIC,
    NavigationPage.DOCUMENTS,
    NavigationPage.PHOTOS,
    NavigationPage.PREFERENCES,
    NavigationPage.ACCOUNTS,
    NavigationPage.GROUPS,
    NavigationPage.VIDEOS,
    NavigationPage.BOOKMARKS,
    NavigationPage.FEEDBACK,
    NavigationPage.SEARCH,
    NavigationPage.STORIES,
    NavigationPage.CLIPS,
    NavigationPage.BIRTHDAYS
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class NavigationPage {
    companion object {
        const val OTHER = 0
        const val FRIENDS = 1
        const val DIALOGS = 2
        const val FEED = 3
        const val MUSIC = 4
        const val DOCUMENTS = 5
        const val PHOTOS = 6
        const val PREFERENCES = 7
        const val ACCOUNTS = 8
        const val GROUPS = 9
        const val VIDEOS = 10
        const val BOOKMARKS = 11
        const val FEEDBACK = 12
        const val SEARCH = 13
        const val STORIES = 14
        const val CLIPS = 15
        const val BIRTHDAYS = 16
    }
}
