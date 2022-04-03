package dev.ragnarok.fenrir.db.column

import android.provider.BaseColumns

object NotificationColumns : BaseColumns {
    const val TABLENAME = "notifications"
    const val TYPE = "type"
    const val DATE = "date"
    const val DATA = "data"
    const val FULL_ID = TABLENAME + "." + BaseColumns._ID
    const val FULL_TYPE = "$TABLENAME.$TYPE"
    const val FULL_DATE = "$TABLENAME.$DATE"
    const val FULL_DATA = "$TABLENAME.$DATA"
}