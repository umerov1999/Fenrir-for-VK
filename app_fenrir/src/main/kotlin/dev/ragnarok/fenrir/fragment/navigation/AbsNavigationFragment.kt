package dev.ragnarok.fenrir.fragment.navigation

import androidx.annotation.IdRes
import androidx.drawerlayout.widget.DrawerLayout
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.base.BaseFragment
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem
import dev.ragnarok.fenrir.model.drawer.IconMenuItem
import dev.ragnarok.fenrir.model.drawer.RecentChat
import dev.ragnarok.fenrir.model.drawer.SectionMenuItem

abstract class AbsNavigationFragment : BaseFragment() {
    abstract fun refreshNavigationItems()
    abstract fun appendRecentChat(recentChat: RecentChat)
    abstract val isSheetOpen: Boolean
    abstract fun openSheet()
    abstract fun closeSheet()
    abstract fun unblockSheet()
    abstract fun blockSheet()
    abstract fun selectPage(item: AbsMenuItem?)
    abstract fun setUp(@IdRes fragmentId: Int, drawerLayout: DrawerLayout)
    abstract fun onUnreadDialogsCountChange(count: Int)
    abstract fun onUnreadNotificationsCountChange(count: Int)
    interface NavigationDrawerCallbacks {
        fun onSheetItemSelected(item: AbsMenuItem, longClick: Boolean)
        fun onSheetClosed()
    }

    companion object {
        const val PAGE_FRIENDS = 0
        const val PAGE_DIALOGS = 1
        const val PAGE_FEED = 2
        const val PAGE_MUSIC = 3
        const val PAGE_DOCUMENTS = 4
        const val PAGE_PHOTOS = 5
        const val PAGE_PREFERENSES = 6
        const val PAGE_ACCOUNTS = 7
        const val PAGE_GROUPS = 8
        const val PAGE_VIDEOS = 9
        const val PAGE_BOOKMARKS = 10
        const val PAGE_NOTIFICATION = 11
        const val PAGE_SEARCH = 12
        const val PAGE_NEWSFEED_COMMENTS = 13

        @JvmField
        val SECTION_ITEM_FRIENDS: SectionMenuItem =
            IconMenuItem(PAGE_FRIENDS, R.drawable.friends, R.string.friends)

        @JvmField
        val SECTION_ITEM_DIALOGS: SectionMenuItem =
            IconMenuItem(PAGE_DIALOGS, R.drawable.email, R.string.dialogs)

        @JvmField
        val SECTION_ITEM_FEED: SectionMenuItem =
            IconMenuItem(PAGE_FEED, R.drawable.rss, R.string.feed)

        @JvmField
        val SECTION_ITEM_FEEDBACK: SectionMenuItem =
            IconMenuItem(PAGE_NOTIFICATION, R.drawable.feed, R.string.drawer_feedback)

        @JvmField
        val SECTION_ITEM_NEWSFEED_COMMENTS: SectionMenuItem = IconMenuItem(
            PAGE_NEWSFEED_COMMENTS,
            R.drawable.comment,
            R.string.drawer_newsfeed_comments
        )

        @JvmField
        val SECTION_ITEM_GROUPS: SectionMenuItem =
            IconMenuItem(PAGE_GROUPS, R.drawable.groups, R.string.groups)

        @JvmField
        val SECTION_ITEM_PHOTOS: SectionMenuItem =
            IconMenuItem(PAGE_PHOTOS, R.drawable.photo_album, R.string.photos)

        @JvmField
        val SECTION_ITEM_VIDEOS: SectionMenuItem =
            IconMenuItem(PAGE_VIDEOS, R.drawable.video, R.string.videos)

        @JvmField
        val SECTION_ITEM_BOOKMARKS: SectionMenuItem =
            IconMenuItem(PAGE_BOOKMARKS, R.drawable.star, R.string.bookmarks)

        @JvmField
        val SECTION_ITEM_AUDIOS: SectionMenuItem =
            IconMenuItem(PAGE_MUSIC, R.drawable.music, R.string.music)

        @JvmField
        val SECTION_ITEM_DOCS: SectionMenuItem =
            IconMenuItem(PAGE_DOCUMENTS, R.drawable.file, R.string.attachment_documents)

        @JvmField
        val SECTION_ITEM_SEARCH: SectionMenuItem =
            IconMenuItem(PAGE_SEARCH, R.drawable.magnify, R.string.search)

        @JvmField
        val SECTION_ITEM_SETTINGS: SectionMenuItem =
            IconMenuItem(PAGE_PREFERENSES, R.drawable.preferences, R.string.settings)

        @JvmField
        val SECTION_ITEM_ACCOUNTS: SectionMenuItem =
            IconMenuItem(PAGE_ACCOUNTS, R.drawable.account_circle, R.string.accounts)
    }
}