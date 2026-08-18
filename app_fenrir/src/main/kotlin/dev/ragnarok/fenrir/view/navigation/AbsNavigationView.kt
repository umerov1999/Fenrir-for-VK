package dev.ragnarok.fenrir.view.navigation

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.drawerlayout.widget.DrawerLayout
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.DrawerCategory
import dev.ragnarok.fenrir.model.NavigationPage
import dev.ragnarok.fenrir.model.SwitchableCategory
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem
import dev.ragnarok.fenrir.model.drawer.IconMenuItem
import dev.ragnarok.fenrir.model.drawer.RecentChat

abstract class AbsNavigationView : FrameLayout {
    abstract fun refreshNavigationItems(list: List<DrawerCategory>? = null)
    abstract fun appendRecentChat(recentChat: RecentChat)
    abstract val isSheetOpen: Boolean
    abstract fun openSheet()
    abstract fun closeSheet()
    abstract fun unblockSheet()
    abstract fun blockSheet()
    abstract fun selectPage(item: AbsMenuItem?)
    abstract fun setUp(drawerLayout: DrawerLayout)
    abstract fun onAccountChange(newAccountId: Long)
    abstract fun onUnreadDialogsCountChange(count: Int)
    abstract fun onUnreadNotificationsCountChange(count: Int)
    abstract fun checkCloseByClick(ev: MotionEvent): Boolean
    interface NavigationDrawerCallbacks {
        fun onSheetItemSelected(item: AbsMenuItem, longClick: Boolean)
        fun onSheetClosed()
    }

    abstract fun setStatesCallback(callback: NavigationStatesCallbacks?)
    interface NavigationStatesCallbacks {
        fun onMove(slideOffset: Float)
        fun onOpened()
        fun onClosed()
        fun closeKeyboard()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    companion object {
        val SECTION_ITEM_OTHER =
            IconMenuItem(NavigationPage.OTHER, R.drawable.ic_menu_24_white, R.string.other)
        val SECTION_ITEM_FRIENDS =
            IconMenuItem(NavigationPage.FRIENDS, R.drawable.friends, R.string.friends)
        val SECTION_ITEM_DIALOGS =
            IconMenuItem(NavigationPage.DIALOGS, R.drawable.email, R.string.dialogs)
        val SECTION_ITEM_FEED = IconMenuItem(NavigationPage.FEED, R.drawable.rss, R.string.feed)
        val SECTION_ITEM_FEEDBACK =
            IconMenuItem(NavigationPage.FEEDBACK, R.drawable.feed, R.string.drawer_feedback)
        val SECTION_ITEM_STORIES = IconMenuItem(
            NavigationPage.STORIES,
            R.drawable.story_outline,
            R.string.stories
        )
        val SECTION_ITEM_CLIPS = IconMenuItem(
            NavigationPage.CLIPS,
            R.drawable.clip_outline,
            R.string.clips
        )

        val SECTION_ITEM_BIRTHDAYS = IconMenuItem(
            NavigationPage.BIRTHDAYS,
            R.drawable.cake,
            R.string.birthdays
        )
        val SECTION_ITEM_GROUPS =
            IconMenuItem(NavigationPage.GROUPS, R.drawable.groups, R.string.groups)
        val SECTION_ITEM_PHOTOS =
            IconMenuItem(NavigationPage.PHOTOS, R.drawable.photo_album, R.string.photos)
        val SECTION_ITEM_VIDEOS =
            IconMenuItem(NavigationPage.VIDEOS, R.drawable.video, R.string.videos)
        val SECTION_ITEM_BOOKMARKS =
            IconMenuItem(NavigationPage.BOOKMARKS, R.drawable.star, R.string.bookmarks)
        val SECTION_ITEM_AUDIOS =
            IconMenuItem(NavigationPage.MUSIC, R.drawable.music, R.string.music)
        val SECTION_ITEM_DOCS =
            IconMenuItem(NavigationPage.DOCUMENTS, R.drawable.file, R.string.attachment_documents)
        val SECTION_ITEM_SEARCH =
            IconMenuItem(NavigationPage.SEARCH, R.drawable.magnify, R.string.search)
        val SECTION_ITEM_SETTINGS =
            IconMenuItem(NavigationPage.PREFERENCES, R.drawable.preferences, R.string.settings)
        val SECTION_ITEM_ACCOUNTS =
            IconMenuItem(NavigationPage.ACCOUNTS, R.drawable.account_circle, R.string.accounts)

        fun getItemBySwitchableCategory(@SwitchableCategory type: String): IconMenuItem? {
            return when (type) {
                SwitchableCategory.FRIENDS -> SECTION_ITEM_FRIENDS
                SwitchableCategory.DIALOGS -> SECTION_ITEM_DIALOGS
                SwitchableCategory.FEED -> SECTION_ITEM_FEED
                SwitchableCategory.FEEDBACK -> SECTION_ITEM_FEEDBACK
                SwitchableCategory.STORIES -> SECTION_ITEM_STORIES
                SwitchableCategory.CLIPS -> SECTION_ITEM_CLIPS
                SwitchableCategory.BIRTHDAYS -> SECTION_ITEM_BIRTHDAYS
                SwitchableCategory.GROUPS -> SECTION_ITEM_GROUPS
                SwitchableCategory.PHOTOS -> SECTION_ITEM_PHOTOS
                SwitchableCategory.VIDEOS -> SECTION_ITEM_VIDEOS
                SwitchableCategory.MUSIC -> SECTION_ITEM_AUDIOS
                SwitchableCategory.DOCS -> SECTION_ITEM_DOCS
                SwitchableCategory.FAVES -> SECTION_ITEM_BOOKMARKS
                SwitchableCategory.SEARCH -> SECTION_ITEM_SEARCH
                SwitchableCategory.SETTINGS -> SECTION_ITEM_SETTINGS
                SwitchableCategory.ACCOUNTS -> SECTION_ITEM_ACCOUNTS
                else -> null
            }
        }

        fun getItemByPageId(@NavigationPage id: Int): IconMenuItem? {
            return when (id) {
                NavigationPage.FRIENDS -> SECTION_ITEM_FRIENDS
                NavigationPage.DIALOGS -> SECTION_ITEM_DIALOGS
                NavigationPage.FEED -> SECTION_ITEM_FEED
                NavigationPage.FEEDBACK -> SECTION_ITEM_FEEDBACK
                NavigationPage.STORIES -> SECTION_ITEM_STORIES
                NavigationPage.CLIPS -> SECTION_ITEM_CLIPS
                NavigationPage.BIRTHDAYS -> SECTION_ITEM_BIRTHDAYS
                NavigationPage.GROUPS -> SECTION_ITEM_GROUPS
                NavigationPage.PHOTOS -> SECTION_ITEM_PHOTOS
                NavigationPage.VIDEOS -> SECTION_ITEM_VIDEOS
                NavigationPage.MUSIC -> SECTION_ITEM_AUDIOS
                NavigationPage.DOCUMENTS -> SECTION_ITEM_DOCS
                NavigationPage.BOOKMARKS -> SECTION_ITEM_BOOKMARKS
                NavigationPage.SEARCH -> SECTION_ITEM_SEARCH
                NavigationPage.PREFERENCES -> SECTION_ITEM_SETTINGS
                NavigationPage.ACCOUNTS -> SECTION_ITEM_ACCOUNTS
                else -> null
            }
        }
    }
}