package dev.ragnarok.fenrir.db.interfaces

import dev.ragnarok.fenrir.db.serialize.ISerializeAdapter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface ITempDataStorage {
    fun <T> getData(
        ownerId: Int,
        sourceId: Int,
        serializer: ISerializeAdapter<T>
    ): Single<List<T>>

    fun <T> put(
        ownerId: Int,
        sourceId: Int,
        data: List<T>,
        serializer: ISerializeAdapter<T>
    ): Completable

    fun delete(ownerId: Int): Completable
    fun clearAll()
}