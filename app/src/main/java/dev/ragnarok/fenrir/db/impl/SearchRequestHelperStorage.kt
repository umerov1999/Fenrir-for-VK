package dev.ragnarok.fenrir.db.impl

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.SearchRequestHelper
import dev.ragnarok.fenrir.db.column.SearchRequestColumns
import dev.ragnarok.fenrir.db.interfaces.ISearchRequestHelperStorage
import dev.ragnarok.fenrir.util.Exestime.log
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single

class SearchRequestHelperStorage internal constructor(context: Context) :
    ISearchRequestHelperStorage {
    private val app: Context = context.applicationContext
    private fun helper(): SearchRequestHelper {
        return SearchRequestHelper.getInstance(app)
    }

    override fun getQueries(sourceId: Int): Single<List<String>> {
        return Single.fromCallable {
            val start = System.currentTimeMillis()
            val where = SearchRequestColumns.SOURCE_ID + " = ?"
            val args = arrayOf(sourceId.toString())
            val cursor = helper().readableDatabase.query(
                SearchRequestColumns.TABLENAME,
                PROJECTION, where, args, null, null, BaseColumns._ID + " DESC"
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

    override fun insertQuery(sourceId: Int, query: String?): Completable {
        if (query == null) {
            return Completable.complete()
        }
        val queryClean = query.trim { it <= ' ' }
        return if (queryClean.isEmpty()) {
            Completable.complete()
        } else Completable.create { emitter: CompletableEmitter ->
            val db = helper().writableDatabase
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

    override fun clearAll() {
        helper().writableDatabase.delete(SearchRequestColumns.TABLENAME, null, null)
    }

    override fun delete(sourceId: Int): Completable {
        return Completable.fromAction {
            val start = System.currentTimeMillis()
            val count = helper().writableDatabase.delete(
                SearchRequestColumns.TABLENAME,
                SearchRequestColumns.SOURCE_ID + " = ?", arrayOf(sourceId.toString())
            )
            log("SearchRequestHelperStorage.delete", start, "count: $count")
        }
    }

    companion object {
        private val PROJECTION = arrayOf(
            BaseColumns._ID, SearchRequestColumns.SOURCE_ID, SearchRequestColumns.QUERY
        )
    }

}