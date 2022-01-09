package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class PeersColumns implements BaseColumns {

    public static final String TABLENAME = "peersnew";
    public static final String UNREAD = "unread";
    public static final String TITLE = "title";
    public static final String IN_READ = "in_read";
    public static final String OUT_READ = "out_read";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String KEYBOARD = "current_keyboard";
    public static final String MAJOR_ID = "major_id";
    public static final String MINOR_ID = "minor_id";
    public static final String PINNED = "pinned";
    public static final String LAST_MESSAGE_ID = "last_message_id";
    public static final String ACL = "acl";
    public static final String IS_GROUP_CHANNEL = "is_group_channel";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_UNREAD = TABLENAME + "." + UNREAD;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_IN_READ = TABLENAME + "." + IN_READ;
    public static final String FULL_OUT_READ = TABLENAME + "." + OUT_READ;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_KEYBOARD = TABLENAME + "." + KEYBOARD;
    public static final String FULL_MAJOR_ID = TABLENAME + "." + MAJOR_ID;
    public static final String FULL_MINOR_ID = TABLENAME + "." + MINOR_ID;
    public static final String FULL_PINNED = TABLENAME + "." + PINNED;
    public static final String FULL_LAST_MESSAGE_ID = TABLENAME + "." + LAST_MESSAGE_ID;
    public static final String FULL_ACL = TABLENAME + "." + ACL;
    public static final String FULL_IS_GROUP_CHANNEL = TABLENAME + "." + IS_GROUP_CHANNEL;

    private PeersColumns() {
    }
}