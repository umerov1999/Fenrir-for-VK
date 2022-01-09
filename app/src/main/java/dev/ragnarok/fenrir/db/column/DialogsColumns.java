package dev.ragnarok.fenrir.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import dev.ragnarok.fenrir.api.model.VKApiChat;
import dev.ragnarok.fenrir.api.model.VKApiMessage;

public final class DialogsColumns implements BaseColumns {

    public static final String TABLENAME = "dialogs";
    public static final String UNREAD = "unread";
    public static final String TITLE = "title";
    public static final String IN_READ = "in_read";
    public static final String OUT_READ = "out_read";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String MAJOR_ID = "major_id";
    public static final String MINOR_ID = "minor_id";
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
    public static final String FULL_LAST_MESSAGE_ID = TABLENAME + "." + LAST_MESSAGE_ID;
    public static final String FULL_ACL = TABLENAME + "." + ACL;
    public static final String FULL_IS_GROUP_CHANNEL = TABLENAME + "." + IS_GROUP_CHANNEL;
    public static final String FULL_MAJOR_ID = TABLENAME + "." + MAJOR_ID;
    public static final String FULL_MINOR_ID = TABLENAME + "." + MINOR_ID;
    public static final String FOREIGN_MESSAGE_FROM_ID = "message_from_id";
    public static final String FOREIGN_MESSAGE_BODY = "message_body";
    public static final String FOREIGN_MESSAGE_DATE = "message_date";
    public static final String FOREIGN_MESSAGE_OUT = "message_out";
    //public static final String FOREIGN_MESSAGE_READ_STATE = "message_read_state";
    public static final String FOREIGN_MESSAGE_HAS_ATTACHMENTS = "message_has_attachments";
    public static final String FOREIGN_MESSAGE_FWD_COUNT = "message_forward_count";
    public static final String FOREIGN_MESSAGE_ACTION = "message_action";
    public static final String FOREIGN_MESSAGE_ENCRYPTED = "message_encrypted";

    private DialogsColumns() {
    }

    public static ContentValues getCV(VKApiChat chat) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, VKApiMessage.CHAT_PEER + chat.id);
        cv.put(TITLE, chat.title);
        cv.put(PHOTO_200, chat.photo_200);
        cv.put(PHOTO_100, chat.photo_100);
        cv.put(PHOTO_50, chat.photo_50);
        return cv;
    }
}