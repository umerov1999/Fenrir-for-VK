package dev.ragnarok.fenrir.db.column

import android.content.ContentValues
import android.provider.BaseColumns
import dev.ragnarok.fenrir.api.model.VKApiChat
import dev.ragnarok.fenrir.model.Peer

object ConversationsColumns : BaseColumns {
    const val TABLENAME = "conversations"
    const val UNREAD = "unread"
    const val TITLE = "title"
    const val IN_READ_CM_ID = "in_read_cm_id"
    const val OUT_READ_CM_ID = "out_read_cm_id"
    const val PHOTO_50 = "photo_50"
    const val PHOTO_100 = "photo_100"
    const val PHOTO_200 = "photo_200"
    const val MAJOR_ID = "major_id"
    const val MINOR_ID = "minor_id"
    const val LAST_CM_ID = "last_cm_id"
    const val ACL = "acl"
    const val IS_GROUP_CHANNEL = "is_group_channel"
    const val FULL_ID = TABLENAME + "." + BaseColumns._ID
    const val FULL_UNREAD = "$TABLENAME.$UNREAD"
    const val FULL_TITLE = "$TABLENAME.$TITLE"
    const val FULL_IN_READ_CM_ID = "$TABLENAME.$IN_READ_CM_ID"
    const val FULL_OUT_READ_CM_ID = "$TABLENAME.$OUT_READ_CM_ID"
    const val FULL_PHOTO_50 = "$TABLENAME.$PHOTO_50"
    const val FULL_PHOTO_100 = "$TABLENAME.$PHOTO_100"
    const val FULL_PHOTO_200 = "$TABLENAME.$PHOTO_200"
    const val FULL_LAST_CM_ID = "$TABLENAME.$LAST_CM_ID"
    const val FULL_ACL = "$TABLENAME.$ACL"
    const val FULL_IS_GROUP_CHANNEL = "$TABLENAME.$IS_GROUP_CHANNEL"
    const val FULL_MAJOR_ID = "$TABLENAME.$MAJOR_ID"
    const val FULL_MINOR_ID = "$TABLENAME.$MINOR_ID"
    const val FOREIGN_MESSAGE_LOCAL_ID = "message_local_id"
    const val FOREIGN_MESSAGE_ID = "message_id"
    const val FOREIGN_MESSAGE_FROM_ID = "message_from_id"
    const val FOREIGN_MESSAGE_TEXT = "message_text"
    const val FOREIGN_MESSAGE_DATE = "message_date"
    const val FOREIGN_MESSAGE_OUT = "message_out"
    const val FOREIGN_MESSAGE_ATTACHMENTS_BLOB = "message_attachments_blob"
    const val FOREIGN_MESSAGE_FWD_BLOB = "message_forward_blob"
    const val FOREIGN_MESSAGE_ACTION = "message_action"
    const val FOREIGN_MESSAGE_IMPORTANT = "message_important"
    const val FOREIGN_MESSAGE_RANDOM_ID = "message_random_id"
    const val FOREIGN_MESSAGE_UPDATE_TIME = "message_update_time"
    const val FOREIGN_MESSAGE_VERSION = "message_version"

    fun getCV(chat: VKApiChat): ContentValues {
        val cv = ContentValues()
        cv.put(BaseColumns._ID, Peer.fromChatId(chat.id))
        cv.put(TITLE, chat.title)
        cv.put(PHOTO_200, chat.photo_200)
        cv.put(PHOTO_100, chat.photo_100)
        cv.put(PHOTO_50, chat.photo_50)
        return cv
    }
}
