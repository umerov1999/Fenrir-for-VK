package dev.ragnarok.fenrir.db.interfaces

import android.content.ContentValues
import dev.ragnarok.fenrir.db.AttachmentFor
import dev.ragnarok.fenrir.db.model.entity.DboEntity
import dev.ragnarok.fenrir.util.Pair
import kotlinx.coroutines.flow.Flow

interface IAttachmentsStorage : IStorage {
    fun remove(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachmentKey: String,
        where: String,
        args: Array<String>? = null
    ): Flow<Boolean>

    fun remove(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachmentKey: String,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean>

    fun removeAll(
        accountId: Long,
        attachmentFor: AttachmentFor,
        where: String,
        args: Array<String>? = null
    ): Flow<Boolean>

    fun removeAll(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean>

    fun getAttachmentsFromBlob(
        accountId: Long,
        attachmentFor: AttachmentFor,
        where: String,
        args: Array<String>? = null
    ): Flow<List<DboEntity>?>

    fun getAttachmentsFromBlob(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<List<DboEntity>?>

    fun getAttachmentsCount(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Int>

    fun getAttachmentsCount(
        accountId: Long,
        attachmentFor: AttachmentFor,
        where: String,
        args: Array<String>? = null
    ): Flow<Int>

    fun attachToBlobSync(
        cv: ContentValues?,
        attachmentFor: AttachmentFor,
        attachmentsEntities: List<DboEntity>?,
        where: String? = null,
        args: Array<String>? = null,
        replace: Boolean = false,
        accountId: Long? = null
    ): Flow<Boolean>

    fun attachToBlobSync(
        cv: ContentValues?,
        attachmentFor: AttachmentFor,
        attachmentsEntities: List<DboEntity>?,
        attachToDbId: Int? = null,
        attachToElement: Pair<Long, Int>?,
        replace: Boolean = false,
        accountId: Long? = null
    ): Flow<Boolean>
}
