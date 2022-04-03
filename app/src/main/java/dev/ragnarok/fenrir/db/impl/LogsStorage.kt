package dev.ragnarok.fenrir.db.impl

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.LogSqliteHelper
import dev.ragnarok.fenrir.db.column.LogColumns
import dev.ragnarok.fenrir.db.interfaces.ILogsStorage
import dev.ragnarok.fenrir.model.LogEvent
import io.reactivex.rxjava3.core.Single

class LogsStorage(context: Context) : ILogsStorage {
    private val context: Context = context.applicationContext
    private fun helper(): LogSqliteHelper {
        return LogSqliteHelper.getInstance(context)
    }

    override fun Clear() {
        helper().Clear()
    }

    override fun add(type: Int, tag: String, body: String): Single<LogEvent> {
        return Single.fromCallable {
            val now = System.currentTimeMillis()
            val cv = ContentValues()
            cv.put(LogColumns.TYPE, type)
            cv.put(LogColumns.TAG, tag)
            cv.put(LogColumns.BODY, body)
            cv.put(LogColumns.DATE, now)
            val id = helper().writableDatabase.insert(LogColumns.TABLENAME, null, cv)
            LogEvent(id.toInt())
                .setBody(body)
                .setTag(tag)
                .setDate(now)
                .setType(type)
        }
    }

    override fun getAll(type: Int): Single<List<LogEvent>> {
        return Single.fromCallable {
            val cursor = helper().readableDatabase.query(
                LogColumns.TABLENAME,
                PROJECTION,
                LogColumns.TYPE + " = ?",
                arrayOf(type.toString()),
                null,
                null,
                BaseColumns._ID + " DESC"
            )
            val data: MutableList<LogEvent> = ArrayList(cursor.count)
            while (cursor.moveToNext()) {
                data.add(map(cursor))
            }
            cursor.close()
            data
        }
    }

    companion object {
        private val PROJECTION = arrayOf(
            BaseColumns._ID,
            LogColumns.TYPE,
            LogColumns.DATE,
            LogColumns.TAG,
            LogColumns.BODY
        )

        private fun map(cursor: Cursor): LogEvent {
            return LogEvent(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                .setType(cursor.getInt(cursor.getColumnIndexOrThrow(LogColumns.TYPE)))
                .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(LogColumns.DATE)))
                .setTag(cursor.getString(cursor.getColumnIndexOrThrow(LogColumns.TAG)))
                .setBody(cursor.getString(cursor.getColumnIndexOrThrow(LogColumns.BODY)))
        }
    }

}