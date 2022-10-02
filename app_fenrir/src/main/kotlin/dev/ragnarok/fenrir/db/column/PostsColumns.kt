package dev.ragnarok.fenrir.db.column

import android.provider.BaseColumns

object PostsColumns : BaseColumns {
    const val TABLENAME = "posts"
    const val POST_ID = "post_id"
    const val OWNER_ID = "owner_id"
    const val FROM_ID = "from_id"
    const val DATE = "date"
    const val TEXT = "text"
    const val REPLY_OWNER_ID = "reply_owner_id"
    const val REPLY_POST_ID = "reply_post_id"
    const val FRIENDS_ONLY = "friends_only"
    const val COMMENTS_COUNT = "comments_count"
    const val CAN_POST_COMMENT = "can_post_comment"
    const val LIKES_COUNT = "likes_count"
    const val USER_LIKES = "user_likes"
    const val CAN_LIKE = "can_like"
    const val CAN_PUBLISH = "can_publish"
    const val CAN_EDIT = "can_edit"
    const val REPOSTS_COUNT = "reposts_count"
    const val USER_REPOSTED = "user_reposted"
    const val POST_TYPE = "post_type"
    const val ATTACHMENTS_MASK = "attachments_mask"
    const val SIGNED_ID = "signer_id"
    const val CREATED_BY = "created_by"
    const val CAN_PIN = "can_pin"
    const val IS_PINNED = "is_pinned"
    const val IS_FAVORITE = "is_favorite"
    const val DELETED = "deleted"
    const val POST_SOURCE = "post_source"
    const val VIEWS = "views"
    const val COPYRIGHT_JSON = "copyright_json"
    const val FULL_ID = TABLENAME + "." + BaseColumns._ID
    const val FULL_POST_ID = "$TABLENAME.$POST_ID"
    const val FULL_OWNER_ID = "$TABLENAME.$OWNER_ID"
    const val FULL_FROM_ID = "$TABLENAME.$FROM_ID"
    const val FULL_DATE = "$TABLENAME.$DATE"
    const val FULL_TEXT = "$TABLENAME.$TEXT"
    const val FULL_COPYRIGHT_JSON = "$TABLENAME.$COPYRIGHT_JSON"
    const val FULL_REPLY_OWNER_ID = "$TABLENAME.$REPLY_OWNER_ID"
    const val FULL_REPLY_POST_ID = "$TABLENAME.$REPLY_POST_ID"
    const val FULL_FRIENDS_ONLY = "$TABLENAME.$FRIENDS_ONLY"
    const val FULL_COMMENTS_COUNT = "$TABLENAME.$COMMENTS_COUNT"
    const val FULL_CAN_POST_COMMENT = "$TABLENAME.$CAN_POST_COMMENT"
    const val FULL_LIKES_COUNT = "$TABLENAME.$LIKES_COUNT"
    const val FULL_USER_LIKES = "$TABLENAME.$USER_LIKES"
    const val FULL_CAN_LIKE = "$TABLENAME.$CAN_LIKE"
    const val FULL_CAN_PUBLISH = "$TABLENAME.$CAN_PUBLISH"
    const val FULL_CAN_EDIT = "$TABLENAME.$CAN_EDIT"
    const val FULL_REPOSTS_COUNT = "$TABLENAME.$REPOSTS_COUNT"
    const val FULL_USER_REPOSTED = "$TABLENAME.$USER_REPOSTED"
    const val FULL_POST_TYPE = "$TABLENAME.$POST_TYPE"
    const val FULL_ATTACHMENTS_MASK = "$TABLENAME.$ATTACHMENTS_MASK"
    const val FULL_SIGNED_ID = "$TABLENAME.$SIGNED_ID"
    const val FULL_CREATED_BY = "$TABLENAME.$CREATED_BY"
    const val FULL_CAN_PIN = "$TABLENAME.$CAN_PIN"
    const val FULL_IS_PINNED = "$TABLENAME.$IS_PINNED"
    const val FULL_DELETED = "$TABLENAME.$DELETED"
    const val FULL_POST_SOURCE = "$TABLENAME.$POST_SOURCE"
    const val FULL_VIEWS = "$TABLENAME.$VIEWS"
    const val FULL_IS_FAVORITE = "$TABLENAME.$IS_FAVORITE"
}