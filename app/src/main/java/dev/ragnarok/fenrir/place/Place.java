package dev.ragnarok.fenrir.place;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import dev.ragnarok.fenrir.util.Objects;

public class Place implements Parcelable {

    public static final int VIDEO_PREVIEW = 1;
    public static final int FRIENDS_AND_FOLLOWERS = 2;
    public static final int EXTERNAL_LINK = 3;
    public static final int DOC_PREVIEW = 4;
    public static final int WALL_POST = 5;
    public static final int COMMENTS = 6;
    public static final int WALL = 7;
    public static final int CONVERSATION_ATTACHMENTS = 8;
    public static final int PLAYER = 9;
    public static final int SEARCH = 10;
    public static final int CHAT = 11;
    public static final int BUILD_NEW_POST = 12;
    public static final int EDIT_COMMENT = 13;
    public static final int EDIT_POST = 14;
    public static final int REPOST = 15;
    public static final int DIALOGS = 16;
    public static final int FORWARD_MESSAGES = 17;
    public static final int TOPICS = 18;
    public static final int CHAT_MEMBERS = 19;
    public static final int COMMUNITIES = 20;
    public static final int LIKES_AND_COPIES = 21;
    public static final int VIDEO_ALBUM = 22;
    public static final int AUDIOS = 23;
    public static final int VIDEOS = 24;
    public static final int VK_PHOTO_ALBUMS = 25;
    public static final int VK_PHOTO_ALBUM = 26;
    public static final int VK_PHOTO_ALBUM_GALLERY = 27;
    public static final int VK_PHOTO_ALBUM_GALLERY_SAVED = 28;

    public static final int FAVE_PHOTOS_GALLERY = 29;
    public static final int SIMPLE_PHOTO_GALLERY = 30;
    public static final int POLL = 31;
    public static final int PREFERENCES = 32;
    public static final int DOCS = 33;
    public static final int FEED = 34;
    public static final int NOTIFICATIONS = 35;
    public static final int BOOKMARKS = 36;
    public static final int RESOLVE_DOMAIN = 37;
    public static final int VK_INTERNAL_PLAYER = 38;
    public static final int NOTIFICATION_SETTINGS = 39;
    public static final int CREATE_PHOTO_ALBUM = 40;
    public static final int EDIT_PHOTO_ALBUM = 41;
    public static final int MESSAGE_LOOKUP = 42;
    public static final int GIF_PAGER = 43;
    public static final int SECURITY = 44;
    public static final int CREATE_POLL = 45;
    public static final int COMMENT_CREATE = 46;
    public static final int LOGS = 47;
    public static final int LOCAL_IMAGE_ALBUM = 48;
    public static final int SINGLE_SEARCH = 49;
    public static final int NEWSFEED_COMMENTS = 50;
    public static final int COMMUNITY_CONTROL = 51;
    public static final int COMMUNITY_BAN_EDIT = 52;
    public static final int COMMUNITY_ADD_BAN = 53;

    public static final int VK_PHOTO_TMP_SOURCE = 54;

    public static final int COMMUNITY_MANAGER_EDIT = 55;
    public static final int COMMUNITY_MANAGER_ADD = 56;

    public static final int REQUEST_EXECUTOR = 57;
    public static final int USER_BLACKLIST = 58;

    public static final int PROXY_ADD = 59;
    public static final int DRAWER_EDIT = 60;
    public static final int SIDE_DRAWER_EDIT = 61;
    public static final int USER_DETAILS = 62;
    public static final int AUDIOS_IN_ALBUM = 63;
    public static final int COMMUNITY_INFO = 64;
    public static final int COMMUNITY_INFO_LINKS = 65;
    public static final int SETTINGS_THEME = 66;
    public static final int SEARCH_BY_AUDIO = 67;
    public static final int MENTIONS = 68;
    public static final int OWNER_ARTICLES = 69;
    public static final int WALL_ATTACHMENTS = 70;
    public static final int STORY_PLAYER = 71;
    public static final int SINGLE_PHOTO = 72;
    public static final int ARTIST = 73;
    public static final int CATALOG_BLOCK_AUDIOS = 74;
    public static final int CATALOG_BLOCK_PLAYLISTS = 75;
    public static final int CATALOG_BLOCK_VIDEOS = 76;
    public static final int CATALOG_BLOCK_LINKS = 77;
    public static final int SHORT_LINKS = 78;
    public static final int IMPORTANT_MESSAGES = 79;
    public static final int MARKET_ALBUMS = 80;
    public static final int MARKETS = 81;
    public static final int MARKET_VIEW = 82;
    public static final int GIFTS = 83;
    public static final int PHOTO_ALL_COMMENT = 84;
    public static final int ALBUMS_BY_VIDEO = 85;
    public static final int FRIENDS_BY_PHONES = 86;
    public static final int UNREAD_MESSAGES = 87;
    public static final int AUDIOS_SEARCH_TABS = 88;
    public static final int GROUP_CHATS = 89;
    public static final int LOCAL_SERVER_PHOTO = 90;
    public static final int VK_PHOTO_ALBUM_GALLERY_NATIVE = 91;
    public static final int SEARCH_COMMENTS = 92;
    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
    private final int type;
    private boolean needFinishMain;
    private String requestListenerKey;
    private FragmentResultListener requestListener;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Bundle args;

    public Place(int type) {
        this.type = type;
    }

    protected Place(Parcel in) {
        type = in.readInt();
        args = in.readBundle(getClass().getClassLoader());
    }

    public void tryOpenWith(@NonNull Context context) {
        if (context instanceof PlaceProvider) {
            ((PlaceProvider) context).openPlace(this);
        }
    }

    public Place setFragmentListener(@NonNull String requestListenerKey, @NonNull FragmentResultListener requestListener) {
        this.requestListenerKey = requestListenerKey;
        this.requestListener = requestListener;
        return this;
    }

    public Place setActivityResultLauncher(@NonNull ActivityResultLauncher<Intent> activityResultLauncher) {
        this.activityResultLauncher = activityResultLauncher;
        return this;
    }

    public boolean isNeedFinishMain() {
        return needFinishMain;
    }

    public Place setNeedFinishMain(boolean needFinishMain) {
        this.needFinishMain = needFinishMain;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeBundle(args);
    }

    public Bundle getArgs() {
        return args;
    }

    public Place setArguments(Bundle arguments) {
        args = arguments;
        return this;
    }

    public Place withStringExtra(String name, String value) {
        prepareArguments().putString(name, value);
        return this;
    }

    public Place withParcelableExtra(String name, Parcelable parcelableExtra) {
        prepareArguments().putParcelable(name, parcelableExtra);
        return this;
    }

    public Place withIntExtra(String name, int value) {
        prepareArguments().putInt(name, value);
        return this;
    }

    public Place withLongExtra(String name, long value) {
        prepareArguments().putLong(name, value);
        return this;
    }

    public Bundle prepareArguments() {
        if (args == null) {
            args = new Bundle();
        }

        return args;
    }

    public void applyFragmentListener(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager) {
        if (Objects.nonNull(requestListener)) {
            fragmentManager.setFragmentResultListener(requestListenerKey, fragment, requestListener);
        }
    }

    public void launchActivityForResult(@NonNull Activity context, @NonNull Intent intent) {
        if (Objects.nonNull(activityResultLauncher) && !needFinishMain) {
            activityResultLauncher.launch(intent);
        } else {
            context.startActivity(intent);
            if (needFinishMain) {
                context.finish();
                context.overridePendingTransition(0, 0);
            }
        }
    }

    public int getType() {
        return type;
    }
}
