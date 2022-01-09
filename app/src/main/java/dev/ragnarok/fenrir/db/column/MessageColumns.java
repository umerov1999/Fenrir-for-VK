package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class MessageColumns implements BaseColumns {

    public static final String TABLENAME = "messages";
    public static final String _ID = "_id";
    public static final String PEER_ID = "peer_id";
    public static final String FROM_ID = "from_id";
    public static final String DATE = "date";
    public static final String OUT = "out";
    public static final String BODY = "body";
    public static final String ENCRYPTED = "encrypted";
    public static final String IMPORTANT = "important";
    public static final String DELETED = "deleted";
    public static final String DELETED_FOR_ALL = "deleted_for_all";
    public static final String FORWARD_COUNT = "fwd_count";
    public static final String HAS_ATTACHMENTS = "has_attachments";
    public static final String STATUS = "status";
    public static final String ATTACH_TO = "attach_to";
    public static final String ORIGINAL_ID = "original_id";
    public static final String UPDATE_TIME = "update_time";
    public static final String PAYLOAD = "payload";
    public static final String KEYBOARD = "keyboard";
    //chat_columns
    public static final String ACTION = "action";
    public static final String ACTION_MID = "action_mid";
    public static final String ACTION_EMAIL = "action_email";
    public static final String ACTION_TEXT = "action_text";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String RANDOM_ID = "random_id";
    public static final String EXTRAS = "extras";
    public static final int DONT_ATTACH = 0;
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_PEER_ID = TABLENAME + "." + PEER_ID;
    public static final String FULL_FROM_ID = TABLENAME + "." + FROM_ID;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_OUT = TABLENAME + "." + OUT;
    public static final String FULL_BODY = TABLENAME + "." + BODY;
    public static final String FULL_ENCRYPTED = TABLENAME + "." + ENCRYPTED;
    public static final String FULL_DELETED = TABLENAME + "." + DELETED;
    public static final String FULL_DELETED_FOR_ALL = TABLENAME + "." + DELETED_FOR_ALL;
    public static final String FULL_IMPORTANT = TABLENAME + "." + IMPORTANT;
    public static final String FULL_FORWARD_COUNT = TABLENAME + "." + FORWARD_COUNT;
    public static final String FULL_HAS_ATTACHMENTS = TABLENAME + "." + HAS_ATTACHMENTS;
    public static final String FULL_STATUS = TABLENAME + "." + STATUS;
    public static final String FULL_ATTACH_TO = TABLENAME + "." + ATTACH_TO;
    public static final String FULL_ORIGINAL_ID = TABLENAME + "." + ORIGINAL_ID;
    public static final String FULL_UPDATE_TIME = TABLENAME + "." + UPDATE_TIME;
    public static final String FULL_ACTION = TABLENAME + "." + ACTION;
    public static final String FULL_ACTION_MID = TABLENAME + "." + ACTION_MID;
    public static final String FULL_ACTION_EMAIL = TABLENAME + "." + ACTION_EMAIL;
    public static final String FULL_ACTION_TEXT = TABLENAME + "." + ACTION_TEXT;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_RANDOM_ID = TABLENAME + "." + RANDOM_ID;
    public static final String FULL_EXTRAS = TABLENAME + "." + EXTRAS;
    public static final String FULL_PAYLOAD = TABLENAME + "." + PAYLOAD;
    public static final String FULL_KEYBOARD = TABLENAME + "." + KEYBOARD;

    private MessageColumns() {
    }
}