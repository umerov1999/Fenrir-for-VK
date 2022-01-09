package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class CommentsColumns implements BaseColumns {

    /* Код комментария, который получен не с сервиса, а создан на устройстве и не отправлен */
    public static final int PROCESSING_COMMENT_ID = -1;
    public static final String TABLENAME = "comments";
    public static final String COMMENT_ID = "comment_id";
    public static final String FROM_ID = "from_id";
    public static final String DATE = "date";
    public static final String TEXT = "text";
    public static final String REPLY_TO_USER = "reply_to_user";
    public static final String THREADS_COUNT = "threads_count";
    public static final String THREADS = "threads";
    public static final String REPLY_TO_COMMENT = "reply_to_comment";
    public static final String LIKES = "likes";
    public static final String USER_LIKES = "user_likes";
    public static final String CAN_LIKE = "can_like";
    public static final String CAN_EDIT = "can_edit";
    public static final String ATTACHMENTS_COUNT = "attachment_count";
    public static final String DELETED = "deleted";
    public static final String SOURCE_ID = "source_id";
    public static final String SOURCE_OWNER_ID = "source_owner_id";
    public static final String SOURCE_TYPE = "source_type";
    public static final String SOURCE_ACCESS_KEY = "source_access_key";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_COMMENT_ID = TABLENAME + "." + COMMENT_ID;
    public static final String FULL_FROM_ID = TABLENAME + "." + FROM_ID;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_TEXT = TABLENAME + "." + TEXT;
    public static final String FULL_REPLY_TO_USER = TABLENAME + "." + REPLY_TO_USER;
    public static final String FULL_REPLY_TO_COMMENT = TABLENAME + "." + REPLY_TO_COMMENT;
    public static final String FULL_THREADS_COUNT = TABLENAME + "." + THREADS_COUNT;
    public static final String FULL_THREADS = TABLENAME + "." + THREADS;
    public static final String FULL_LIKES = TABLENAME + "." + LIKES;
    public static final String FULL_USER_LIKES = TABLENAME + "." + USER_LIKES;
    public static final String FULL_CAN_LIKE = TABLENAME + "." + CAN_LIKE;
    public static final String FULL_CAN_EDIT = TABLENAME + "." + CAN_EDIT;
    public static final String FULL_ATTACHMENTS_COUNT = TABLENAME + "." + ATTACHMENTS_COUNT;
    public static final String FULL_DELETED = TABLENAME + "." + DELETED;
    public static final String FULL_SOURCE_ID = TABLENAME + "." + SOURCE_ID;
    public static final String FULL_SOURCE_OWNER_ID = TABLENAME + "." + SOURCE_OWNER_ID;
    public static final String FULL_SOURCE_TYPE = TABLENAME + "." + SOURCE_TYPE;
    public static final String FULL_SOURCE_ACCESS_KEY = TABLENAME + "." + SOURCE_ACCESS_KEY;

    private CommentsColumns() {
    }
}