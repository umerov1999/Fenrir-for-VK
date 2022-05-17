package dev.ragnarok.fenrir.db.impl

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.TempDataHelper
import dev.ragnarok.fenrir.db.column.LogColumns
import dev.ragnarok.fenrir.db.column.SearchRequestColumns
import dev.ragnarok.fenrir.db.column.ShortcutColumns
import dev.ragnarok.fenrir.db.column.TempDataColumns
import dev.ragnarok.fenrir.db.interfaces.ITempDataStorage
import dev.ragnarok.fenrir.db.serialize.ISerializeAdapter
import dev.ragnarok.fenrir.getInt
import dev.ragnarok.fenrir.getLong
import dev.ragnarok.fenrir.getString
import dev.ragnarok.fenrir.model.LogEvent
import dev.ragnarok.fenrir.model.ShortcutStored
import dev.ragnarok.fenrir.util.Exestime.log
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single

class TempDataStorage internal constructor(context: Context) : ITempDataStorage {
    private val app: Context = context.applicationContext
    private val helper: TempDataHelper by lazy {
        TempDataHelper(app)
    }

    override fun <T> getTemporaryData(
        ownerId: Int,
        sourceId: Int,
        serializer: ISerializeAdapter<T>
    ): Single<List<T>> {
        return Single.fromCallable {
            val start = System.currentTimeMillis()
            val where = TempDataColumns.OWNER_ID + " = ? AND " + TempDataColumns.SOURCE_ID + " = ?"
            val args = arrayOf(ownerId.toString(), sourceId.toString())
            val cursor = helper.readableDatabase.query(
                TempDataColumns.TABLENAME,
                PROJECTION_TEMPORARY, where, args, null, null, null
            )
            val data: MutableList<T> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    val raw = it.getBlob(3)
                    data.add(serializer.deserialize(raw))
                }
            }
            log("TempDataStorage.getData", start, "count: " + data.size)
            data
        }
    }

    override fun <T> putTemporaryData(
        ownerId: Int,
        sourceId: Int,
        data: List<T>,
        serializer: ISerializeAdapter<T>
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val start = System.currentTimeMillis()
            val db = helper.writableDatabase
            db.beginTransaction()
            try {
                // clear
                db.delete(
                    TempDataColumns.TABLENAME,
                    TempDataColumns.OWNER_ID + " = ? AND " + TempDataColumns.SOURCE_ID + " = ?",
                    arrayOf(ownerId.toString(), sourceId.toString())
                )
                for (t in data) {
                    if (emitter.isDisposed) {
                        break
                    }
                    val cv = ContentValues()
                    cv.put(TempDataColumns.OWNER_ID, ownerId)
                    cv.put(TempDataColumns.SOURCE_ID, sourceId)
                    cv.put(TempDataColumns.DATA, serializer.serialize(t))
                    db.insert(TempDataColumns.TABLENAME, null, cv)
                }
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            log("TempDataStorage.put", start, "count: " + data.size)
            emitter.onComplete()
        }
    }

    override fun deleteTemporaryData(ownerId: Int): Completable {
        return Completable.fromAction {
            val start = System.currentTimeMillis()
            val count = helper.writableDatabase.delete(
                TempDataColumns.TABLENAME,
                TempDataColumns.OWNER_ID + " = ?", arrayOf(ownerId.toString())
            )
            log("TempDataStorage.delete", start, "count: $count")
        }
    }

    override fun getSearchQueries(sourceId: Int): Single<List<String>> {
        return Single.fromCallable {
            val start = System.currentTimeMillis()
            val where = SearchRequestColumns.SOURCE_ID + " = ?"
            val args = arrayOf(sourceId.toString())
            val cursor = helper.readableDatabase.query(
                SearchRequestColumns.TABLENAME,
                PROJECTION_SEARCH, where, args, null, null, BaseColumns._ID + " DESC"
            )
            val data: MutableList<String> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    data.add(it.getString(2))
                }
            }
            log("SearchRequestHelperStorage.getQueries", start, "count: " + data.size)
            data
        }
    }

    override fun insertSearchQuery(sourceId: Int, query: String?): Completable {
        if (query == null) {
            return Completable.complete()
        }
        val queryClean = query.trim { it <= ' ' }
        return if (queryClean.isEmpty()) {
            Completable.complete()
        } else Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            db.delete(
                SearchRequestColumns.TABLENAME,
                SearchRequestColumns.QUERY + " = ?", arrayOf(queryClean)
            )
            try {
                val cv = ContentValues()
                cv.put(SearchRequestColumns.SOURCE_ID, sourceId)
                cv.put(SearchRequestColumns.QUERY, queryClean)
                db.insert(SearchRequestColumns.TABLENAME, null, cv)
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun deleteSearch(sourceId: Int): Completable {
        return Completable.fromAction {
            val start = System.currentTimeMillis()
            val count = helper.writableDatabase.delete(
                SearchRequestColumns.TABLENAME,
                SearchRequestColumns.SOURCE_ID + " = ?", arrayOf(sourceId.toString())
            )
            log("SearchRequestHelperStorage.delete", start, "count: $count")
        }
    }

    override fun addShortcut(action: String, cover: String, name: String): Completable {
        return Completable.create {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (it.isDisposed) {
                db.endTransaction()
                it.onComplete()
                return@create
            }
            db.delete(
                ShortcutColumns.TABLENAME,
                ShortcutColumns.ACTION + " = ?", arrayOf(action)
            )
            try {
                val cv = ContentValues()
                cv.put(ShortcutColumns.ACTION, action)
                cv.put(ShortcutColumns.NAME, name)
                cv.put(ShortcutColumns.COVER, cover)
                db.insert(ShortcutColumns.TABLENAME, null, cv)
                if (!it.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            it.onComplete()
        }
    }

    override fun addShortcuts(list: List<ShortcutStored>): Completable {
        return Completable.create {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (it.isDisposed) {
                db.endTransaction()
                it.onComplete()
                return@create
            }
            try {
                for (i in list) {
                    db.delete(
                        ShortcutColumns.TABLENAME,
                        ShortcutColumns.ACTION + " = ?", arrayOf(i.action)
                    )
                    val cv = ContentValues()
                    cv.put(ShortcutColumns.ACTION, i.action)
                    cv.put(ShortcutColumns.NAME, i.name)
                    cv.put(ShortcutColumns.COVER, i.cover)
                    db.insert(ShortcutColumns.TABLENAME, null, cv)
                }
                if (!it.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            it.onComplete()
        }
    }

    override fun deleteShortcut(action: String): Completable {
        return Completable.fromAction {
            val start = System.currentTimeMillis()
            val count = helper.writableDatabase.delete(
                ShortcutColumns.TABLENAME,
                ShortcutColumns.ACTION + " = ?", arrayOf(action)
            )
            log("SearchRequestHelperStorage.delete", start, "count: $count")
        }
    }

    override fun getShortcutAll(): Single<List<ShortcutStored>> {
        return Single.fromCallable {
            val cursor = helper.readableDatabase.query(
                ShortcutColumns.TABLENAME,
                PROJECTION_SHORTCUT,
                null,
                null,
                null,
                null,
                BaseColumns._ID + " DESC"
            )
            val data: MutableList<ShortcutStored> = ArrayList(cursor.count)
            while (cursor.moveToNext()) {
                data.add(mapShortcut(cursor))
            }
            cursor.close()
            data
        }
    }

    override fun addLog(type: Int, tag: String, body: String): Single<LogEvent> {
        return Single.fromCallable {
            val now = System.currentTimeMillis()
            val cv = ContentValues()
            cv.put(LogColumns.TYPE, type)
            cv.put(LogColumns.TAG, tag)
            cv.put(LogColumns.BODY, body)
            cv.put(LogColumns.DATE, now)
            val id = helper.writableDatabase.insert(LogColumns.TABLENAME, null, cv)
            LogEvent(id.toInt())
                .setBody(body)
                .setTag(tag)
                .setDate(now)
                .setType(type)
        }
    }

    override fun getLogAll(type: Int): Single<List<LogEvent>> {
        return Single.fromCallable {
            val cursor = helper.readableDatabase.query(
                LogColumns.TABLENAME,
                PROJECTION_LOG,
                LogColumns.TYPE + " = ?",
                arrayOf(type.toString()),
                null,
                null,
                BaseColumns._ID + " DESC"
            )
            val data: MutableList<LogEvent> = ArrayList(cursor.count)
            while (cursor.moveToNext()) {
                data.add(mapLog(cursor))
            }
            cursor.close()
            data
        }
    }

    companion object {
        private val PROJECTION_TEMPORARY = arrayOf(
            BaseColumns._ID,
            TempDataColumns.OWNER_ID,
            TempDataColumns.SOURCE_ID,
            TempDataColumns.DATA
        )
        private val PROJECTION_SEARCH = arrayOf(
            BaseColumns._ID, SearchRequestColumns.SOURCE_ID, SearchRequestColumns.QUERY
        )
        private val PROJECTION_SHORTCUT = arrayOf(
            BaseColumns._ID, ShortcutColumns.ACTION, ShortcutColumns.COVER, ShortcutColumns.NAME
        )
        private val PROJECTION_LOG = arrayOf(
            BaseColumns._ID,
            LogColumns.TYPE,
            LogColumns.DATE,
            LogColumns.TAG,
            LogColumns.BODY
        )

        private fun mapLog(cursor: Cursor): LogEvent {
            return LogEvent(cursor.getInt(BaseColumns._ID))
                .setType(cursor.getInt(LogColumns.TYPE))
                .setDate(cursor.getLong(LogColumns.DATE))
                .setTag(cursor.getString(LogColumns.TAG))
                .setBody(cursor.getString(LogColumns.BODY))
        }

        private fun mapShortcut(cursor: Cursor): ShortcutStored {
            return ShortcutStored()
                .setAction(cursor.getString(ShortcutColumns.ACTION)!!)
                .setName(cursor.getString(ShortcutColumns.NAME)!!)
                .setCover(cursor.getString(ShortcutColumns.COVER)!!)
        }
    }
}