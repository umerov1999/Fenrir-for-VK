package dev.ragnarok.fenrir.fragment;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem;
import dev.ragnarok.fenrir.model.drawer.IconMenuItem;
import dev.ragnarok.fenrir.model.drawer.RecentChat;
import dev.ragnarok.fenrir.model.drawer.SectionMenuItem;

public abstract class AbsNavigationFragment extends BaseFragment {
    public static final int PAGE_FRIENDS = 0;
    public static final int PAGE_DIALOGS = 1;
    public static final int PAGE_FEED = 2;
    public static final int PAGE_MUSIC = 3;
    public static final int PAGE_DOCUMENTS = 4;
    public static final int PAGE_PHOTOS = 5;
    public static final int PAGE_PREFERENSES = 6;
    public static final int PAGE_ACCOUNTS = 7;
    public static final int PAGE_GROUPS = 8;
    public static final int PAGE_VIDEOS = 9;
    public static final int PAGE_BOOKMARKS = 10;
    public static final int PAGE_NOTIFICATION = 11;
    public static final int PAGE_SEARCH = 12;
    public static final int PAGE_NEWSFEED_COMMENTS = 13;

    public static final SectionMenuItem SECTION_ITEM_FRIENDS = new IconMenuItem(PAGE_FRIENDS, R.drawable.friends, R.string.friends);
    public static final SectionMenuItem SECTION_ITEM_DIALOGS = new IconMenuItem(PAGE_DIALOGS, R.drawable.email, R.string.dialogs);
    public static final SectionMenuItem SECTION_ITEM_FEED = new IconMenuItem(PAGE_FEED, R.drawable.rss, R.string.feed);
    public static final SectionMenuItem SECTION_ITEM_FEEDBACK = new IconMenuItem(PAGE_NOTIFICATION, R.drawable.feed, R.string.drawer_feedback);
    public static final SectionMenuItem SECTION_ITEM_NEWSFEED_COMMENTS = new IconMenuItem(PAGE_NEWSFEED_COMMENTS, R.drawable.comment, R.string.drawer_newsfeed_comments);
    public static final SectionMenuItem SECTION_ITEM_GROUPS = new IconMenuItem(PAGE_GROUPS, R.drawable.groups, R.string.groups);
    public static final SectionMenuItem SECTION_ITEM_PHOTOS = new IconMenuItem(PAGE_PHOTOS, R.drawable.photo_album, R.string.photos);
    public static final SectionMenuItem SECTION_ITEM_VIDEOS = new IconMenuItem(PAGE_VIDEOS, R.drawable.video, R.string.videos);
    public static final SectionMenuItem SECTION_ITEM_BOOKMARKS = new IconMenuItem(PAGE_BOOKMARKS, R.drawable.star, R.string.bookmarks);
    public static final SectionMenuItem SECTION_ITEM_AUDIOS = new IconMenuItem(PAGE_MUSIC, R.drawable.music, R.string.music);
    public static final SectionMenuItem SECTION_ITEM_DOCS = new IconMenuItem(PAGE_DOCUMENTS, R.drawable.file, R.string.attachment_documents);
    public static final SectionMenuItem SECTION_ITEM_SEARCH = new IconMenuItem(PAGE_SEARCH, R.drawable.magnify, R.string.search);

    public static final SectionMenuItem SECTION_ITEM_SETTINGS = new IconMenuItem(PAGE_PREFERENSES, R.drawable.preferences, R.string.settings);
    public static final SectionMenuItem SECTION_ITEM_ACCOUNTS = new IconMenuItem(PAGE_ACCOUNTS, R.drawable.account_circle, R.string.accounts);

    public abstract void refreshNavigationItems();

    public abstract void appendRecentChat(@NonNull RecentChat recentChat);

    public abstract boolean isSheetOpen();

    public abstract void openSheet();

    public abstract void closeSheet();

    public abstract void unblockSheet();

    public abstract void blockSheet();

    public abstract void selectPage(AbsMenuItem item);

    public abstract void setUp(@IdRes int fragmentId, @NonNull DrawerLayout drawerLayout);

    public abstract void onUnreadDialogsCountChange(int count);

    public abstract void onUnreadNotificationsCountChange(int count);

    public interface NavigationDrawerCallbacks {
        void onSheetItemSelected(AbsMenuItem item, boolean longClick);

        void onSheetClosed();
    }
}
