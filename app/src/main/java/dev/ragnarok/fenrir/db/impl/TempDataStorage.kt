package dev.ragnarok.fenrir.db.impl

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.TempDataHelper
import dev.ragnarok.fenrir.db.column.TempDataColumns
import dev.ragnarok.fenrir.db.interfaces.ITempDataStorage
import dev.ragnarok.fenrir.db.serialize.ISerializeAdapter
import dev.ragnarok.fenrir.util.Exestime.log
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single

class TempDataStorage internal constructor(context: Context) : ITempDataStorage {
    private val app: Context = context.applicationContext
    private fun helper(): TempDataHelper {
        return TempDataHelper.getInstance(app)
    }

    override fun <T> getData(
        ownerId: Int,
        sourceId: Int,
        serializer: ISerializeAdapter<T>
    ): Single<List<T>> {
        return Single.fromCallable {
            val start = System.currentTimeMillis()
            val where = TempDataColumns.OWNER_ID + " = ? AND " + TempDataColumns.SOURCE_ID + " = ?"
            val args = arrayOf(ownerId.toString(), sourceId.toString())
            val cursor = helper().readableDatabase.query(
                TempDataColumns.TABLENAME,
                PROJECTION, where, args, null, null, null
            )
            val data: MutableList<T> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    val raw = it.getString(3)
                    data.add(serializer.deserialize(raw))
                }
            }
            log("TempDataStorage.getData", start, "count: " + data.size)
            data
        }
    }

    override fun <T> put(
        ownerId: Int,
        sourceId: Int,
        data: List<T>,
        serializer: ISerializeAdapter<T>
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val start = System.currentTimeMillis()
            val db = helper().writableDatabase
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

    override fun clearAll() {
        helper().writableDatabase.delete(TempDataColumns.TABLENAME, null, null)
    }

    override fun delete(ownerId: Int): Completable {
        return Completable.fromAction {
            val start = System.currentTimeMillis()
            val count = helper().writableDatabase.delete(
                TempDataColumns.TABLENAME,
                TempDataColumns.OWNER_ID + " = ?", arrayOf(ownerId.toString())
            )
            log("TempDataStorage.delete", start, "count: $count")
        }
    }

    companion object {
        private val PROJECTION = arrayOf(
            BaseColumns._ID,
            TempDataColumns.OWNER_ID,
            TempDataColumns.SOURCE_ID,
            TempDataColumns.DATA
        )
    }

}