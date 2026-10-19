package dev.ragnarok.fenrir.db.interfaces

import dev.ragnarok.fenrir.db.AttachToType
import dev.ragnarok.fenrir.db.model.entity.DboEntity
import dev.ragnarok.fenrir.util.Pair
import kotlinx.coroutines.flow.Flow

interface IAttachmentsStorage : IStorage {
    fun remove(
        accountId: Long,
        @AttachToType attachToType: Int,
        generatedAttachmentId: Int
    ): Flow<Boolean>

    fun removeAll(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int
    ): Flow<Boolean>

    fun attachDbos(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        entities: List<DboEntity>
    ): Flow<IntArray>

    fun getCount(accountId: Long, @AttachToType attachToType: Int, attachToDbId: Int): Flow<Int>
    fun getAttachmentsDbosWithIds(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int
    ): Flow<List<Pair<Int, DboEntity>>>

    suspend fun getAttachmentsDbosSync(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        cancelable: Cancelable
    ): MutableList<DboEntity>
}
