package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class PostsColumns implements BaseColumns {

    public static final String TABLENAME = "posts";
    public static final String POST_ID = "post_id";
    public static final String OWNER_ID = "owner_id";
    public static final String FROM_ID = "from_id";
    public static final String DATE = "date";
    public static final String TEXT = "text";
    public static final String REPLY_OWNER_ID = "reply_owner_id";
    public static final String REPLY_POST_ID = "reply_post_id";
    public static final String FRIENDS_ONLY = "friends_only";
    public static final String COMMENTS_COUNT = "comments_count";
    public static final String CAN_POST_COMMENT = "can_post_comment";
    public static final String LIKES_COUNT = "likes_count";
    public static final String USER_LIKES = "user_likes";
    public static final String CAN_LIKE = "can_like";
    public static final String CAN_PUBLISH = "can_publish";
    public static final String CAN_EDIT = "can_edit";
    public static final String REPOSTS_COUNT = "reposts_count";
    public static final String USER_REPOSTED = "user_reposted";
    public static final String POST_TYPE = "post_type";
    public static final String ATTACHMENTS_MASK = "attachments_mask";
    public static final String SIGNED_ID = "signer_id";
    public static final String CREATED_BY = "created_by";
    public static final String CAN_PIN = "can_pin";
    public static final String IS_PINNED = "is_pinned";
    public static final String DELETED = "deleted";
    public static final String POST_SOURCE = "post_source";
    public static final String VIEWS = "views";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_POST_ID = TABLENAME + "." + POST_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_FROM_ID = TABLENAME + "." + FROM_ID;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_TEXT = TABLENAME + "." + TEXT;
    public static final String FULL_REPLY_OWNER_ID = TABLENAME + "." + REPLY_OWNER_ID;
    public static final String FULL_REPLY_POST_ID = TABLENAME + "." + REPLY_POST_ID;
    public static final String FULL_FRIENDS_ONLY = TABLENAME + "." + FRIENDS_ONLY;
    public static final String FULL_COMMENTS_COUNT = TABLENAME + "." + COMMENTS_COUNT;
    public static final String FULL_CAN_POST_COMMENT = TABLENAME + "." + CAN_POST_COMMENT;
    public static final String FULL_LIKES_COUNT = TABLENAME + "." + LIKES_COUNT;
    public static final String FULL_USER_LIKES = TABLENAME + "." + USER_LIKES;
    public static final String FULL_CAN_LIKE = TABLENAME + "." + CAN_LIKE;
    public static final String FULL_CAN_PUBLISH = TABLENAME + "." + CAN_PUBLISH;
    public static final String FULL_CAN_EDIT = TABLENAME + "." + CAN_EDIT;
    public static final String FULL_REPOSTS_COUNT = TABLENAME + "." + REPOSTS_COUNT;
    public static final String FULL_USER_REPOSTED = TABLENAME + "." + USER_REPOSTED;
    public static final String FULL_POST_TYPE = TABLENAME + "." + POST_TYPE;
    public static final String FULL_ATTACHMENTS_MASK = TABLENAME + "." + ATTACHMENTS_MASK;
    public static final String FULL_SIGNED_ID = TABLENAME + "." + SIGNED_ID;
    public static final String FULL_CREATED_BY = TABLENAME + "." + CREATED_BY;
    public static final String FULL_CAN_PIN = TABLENAME + "." + CAN_PIN;
    public static final String FULL_IS_PINNED = TABLENAME + "." + IS_PINNED;
    public static final String FULL_DELETED = TABLENAME + "." + DELETED;
    public static final String FULL_POST_SOURCE = TABLENAME + "." + POST_SOURCE;
    public static final String FULL_VIEWS = TABLENAME + "." + VIEWS;

    private PostsColumns() {
    }

    //public static final String FULL_HAS_COPY_HISTORY = TABLENAME + "." + HAS_COPY_HISTORY;
    //public static final String FULL_COPY_HISTORY_OF = TABLENAME + "." + COPY_HISTORY_OF;
    //public static final String FULL_COPY_PARENT_TYPE = TABLENAME + "." + COPY_PARENT_TYPE;
}