package dev.ragnarok.fenrir.db.interfaces

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface ISearchRequestHelperStorage {
    fun getQueries(sourceId: Int): Single<List<String>>
    fun insertQuery(sourceId: Int, query: String?): Completable
    fun delete(sourceId: Int): Completable
    fun clearAll()
}