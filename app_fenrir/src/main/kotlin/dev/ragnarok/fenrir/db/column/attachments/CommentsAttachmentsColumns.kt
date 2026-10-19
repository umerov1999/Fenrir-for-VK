package dev.ragnarok.fenrir.db.column.attachments

import android.provider.BaseColumns

object CommentsAttachmentsColumns : BaseColumns {
    const val TABLENAME = "comments_attachments"
    const val C_ID = "comment_id"
    const val C_DATA = "comments_attachment_data"
    const val FULL_ID = TABLENAME + "." + BaseColumns._ID
    const val FULL_C_ID = "$TABLENAME.$C_ID"
    const val FULL_C_DATA = "$TABLENAME.$C_DATA"
}
