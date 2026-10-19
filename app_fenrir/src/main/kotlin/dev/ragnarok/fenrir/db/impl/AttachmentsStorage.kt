package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.AttachmentFor
import dev.ragnarok.fenrir.db.FenrirContentProvider
import dev.ragnarok.fenrir.db.FenrirContentProvider.Companion.getCommentsContentUriFor
import dev.ragnarok.fenrir.db.FenrirContentProvider.Companion.getMessageContentUriFor
import dev.ragnarok.fenrir.db.FenrirContentProvider.Companion.getPostsContentUriFor
import dev.ragnarok.fenrir.db.column.CommentsColumns
import dev.ragnarok.fenrir.db.column.MessagesColumns
import dev.ragnarok.fenrir.db.column.PostsColumns
import dev.ragnarok.fenrir.db.interfaces.IAttachmentsStorage
import dev.ragnarok.fenrir.db.model.entity.DboEntity
import dev.ragnarok.fenrir.getBlob
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.msgpack.MsgPack

internal class AttachmentsStorage(base: AppStorages) : AbsStorage(base), IAttachmentsStorage {
    override fun getAttachmentsFromBlob(
        accountId: Long,
        attachmentFor: AttachmentFor,
        where: String,
        args: Array<String>?
    ): Flow<List<DboEntity>?> {
        return flow {
            val uri = uriForType(attachmentFor, accountId)
            val cursor = contentResolver.query(
                uri,
                arrayOf(dataColumnFor(attachmentFor)),
                where,
                args,
                null
            )
            var ret: List<DboEntity>? = null
            if (isActive()) {
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        ret = mapAttachmentsFromBlob(cursor, attachmentFor)
                    }
                    cursor.close()
                }
            }
            emit(ret)
        }
    }

    override fun getAttachmentsFromBlob(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<List<DboEntity>?> {
        return getAttachmentsFromBlob(
            accountId,
            attachmentFor,
            where = if (attachToElement != null) "${elementOwnerIdForType(attachmentFor)} = ? AND ${
                elementIdForType(
                    attachmentFor
                )
            } = ?" else "${BaseColumns._ID} = ?",
            args = if (attachToElement != null) arrayOf(
                attachToElement.first.toString(),
                attachToElement.second.toString()
            ) else arrayOf(attachToDbId.toString())
        )
    }

    override fun remove(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachmentKey: String,
        where: String,
        args: Array<String>?
    ): Flow<Boolean> {
        return flow {
            val uri = uriForType(attachmentFor, accountId)
            val cursor = contentResolver.query(
                uri,
                arrayOf(dataColumnFor(attachmentFor)),
                where,
                args,
                null
            )
            var result: List<DboEntity>? = null
            if (isActive()) {
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        result = mapAttachmentsFromBlob(cursor, attachmentFor)
                    }
                    cursor.close()
                }
            }
            if (result.nonNullNoEmpty()) {
                val res = ArrayList(result)
                for (i in res.indices) {
                    if (res[i].getAttachmentKey() == attachmentKey) {
                        res.removeAt(i)
                        break
                    }
                }
                emit(res)
            } else {
                emit(null)
            }
        }.flatMapConcat {
            attachToBlobSync(
                cv = null,
                attachmentFor = attachmentFor,
                attachmentsEntities = it,
                where = where,
                args = args,
                replace = true,
                accountId = accountId
            )
        }
    }

    override fun remove(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachmentKey: String,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean> {
        return remove(
            accountId = accountId,
            attachmentFor = attachmentFor,
            attachmentKey = attachmentKey,
            where = if (attachToElement != null) "${elementOwnerIdForType(attachmentFor)} = ? AND ${
                elementIdForType(
                    attachmentFor
                )
            } = ?" else "${BaseColumns._ID} = ?",
            args = if (attachToElement != null) arrayOf(
                attachToElement.first.toString(),
                attachToElement.second.toString()
            ) else arrayOf(attachToDbId.toString())
        )
    }

    override fun removeAll(
        accountId: Long,
        attachmentFor: AttachmentFor,
        where: String,
        args: Array<String>?
    ): Flow<Boolean> {
        return flow {
            val cv = ContentValues()
            cv.putNull(dataColumnFor(attachmentFor))
            val operations = ArrayList<ContentProviderOperation>()
            operations.add(
                ContentProviderOperation.newUpdate(uriForType(attachmentFor, accountId))
                    .withSelection(where, args).build()
            )
            contentResolver.applyBatch(FenrirContentProvider.AUTHORITY, operations)
            emit(true)
        }
    }

    override fun removeAll(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean> {
        return removeAll(
            accountId,
            attachmentFor,
            where = if (attachToElement != null) "${elementOwnerIdForType(attachmentFor)} = ? AND ${
                elementIdForType(
                    attachmentFor
                )
            } = ?" else "${BaseColumns._ID} = ?",
            args = if (attachToElement != null) arrayOf(
                attachToElement.first.toString(),
                attachToElement.second.toString()
            ) else arrayOf(attachToDbId.toString())
        )
    }

    override fun getAttachmentsCount(
        accountId: Long,
        attachmentFor: AttachmentFor,
        where: String,
        args: Array<String>?
    ): Flow<Int> {
        return flow {
            val uri = uriForType(attachmentFor, accountId)
            val cursor = contentResolver.query(
                uri,
                arrayOf(dataColumnFor(attachmentFor)),
                where,
                args,
                null
            )
            var count = 0
            if (isActive()) {
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        count = mapAttachmentsFromBlob(cursor, attachmentFor)?.size.orZero()
                    }
                    cursor.close()
                }
            }
            emit(count)
        }
    }

    override fun getAttachmentsCount(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Int> {
        return getAttachmentsCount(
            accountId,
            attachmentFor,
            where = if (attachToElement != null) "${elementOwnerIdForType(attachmentFor)} = ? AND ${
                elementIdForType(
                    attachmentFor
                )
            } = ?" else "${BaseColumns._ID} = ?",
            args = if (attachToElement != null) arrayOf(
                attachToElement.first.toString(),
                attachToElement.second.toString()
            ) else arrayOf(attachToDbId.toString())
        )
    }

    override fun attachToBlobSync(
        cv: ContentValues?,
        attachmentFor: AttachmentFor,
        attachmentsEntities: List<DboEntity>?,
        where: String?,
        args: Array<String>?,
        replace: Boolean,
        accountId: Long?
    ): Flow<Boolean> {
        return flow {
            val tmpAttachmentsEntities = ArrayList(attachmentsEntities.orEmpty())
            if (!replace && where.nonNullNoEmpty() && accountId != null) {
                val uri = uriForType(attachmentFor, accountId)
                val cursor = contentResolver.query(
                    uri,
                    arrayOf(dataColumnFor(attachmentFor)),
                    where,
                    args,
                    null
                )
                if (isActive()) {
                    if (cursor != null) {
                        if (cursor.moveToNext()) {
                            val attachmentsEntity = mapAttachmentsFromBlob(cursor, attachmentFor)
                            if (attachmentsEntity.nonNullNoEmpty()) {
                                tmpAttachmentsEntities.addAll(0, attachmentsEntity)
                            }
                        }
                        cursor.close()
                    }
                }
            }
            val tmpCv = cv ?: if (where.nonNullNoEmpty() && accountId != null) {
                ContentValues()
            } else {
                throw UnsupportedOperationException()
            }

            putAttachmentsToBlob(tmpCv, attachmentFor, tmpAttachmentsEntities)

            if (cv == null && where.nonNullNoEmpty() && accountId != null) {
                val operations = ArrayList<ContentProviderOperation>()
                operations.add(
                    ContentProviderOperation.newUpdate(uriForType(attachmentFor, accountId))
                        .withSelection(where, args).withValues(tmpCv).build()
                )
                contentResolver.applyBatch(FenrirContentProvider.AUTHORITY, operations)
            }
            emit(true)
        }
    }

    override fun attachToBlobSync(
        cv: ContentValues?,
        attachmentFor: AttachmentFor,
        attachmentsEntities: List<DboEntity>?,
        attachToDbId: Int?,
        attachToElement: Pair<Long, Int>?,
        replace: Boolean,
        accountId: Long?
    ): Flow<Boolean> {
        return if (attachToDbId == null && attachToElement == null) {
            attachToBlobSync(
                cv = cv,
                attachmentFor = attachmentFor,
                attachmentsEntities = attachmentsEntities,
                where = null,
                args = null,
                replace = replace,
                accountId = accountId
            )
        } else {
            attachToBlobSync(
                cv = cv,
                attachmentFor = attachmentFor,
                attachmentsEntities = attachmentsEntities,
                where = if (attachToElement != null) "${elementOwnerIdForType(attachmentFor)} = ? AND ${
                    elementIdForType(
                        attachmentFor
                    )
                } = ?" else "${BaseColumns._ID} = ?",
                args = if (attachToElement != null) arrayOf(
                    attachToElement.first.toString(),
                    attachToElement.second.toString()
                ) else arrayOf(attachToDbId.toString()),
                replace = replace,
                accountId = accountId
            )
        }
    }

    companion object {
        fun mapAttachmentsFromBlobWithColumn(
            cursor: Cursor,
            column: String
        ): List<DboEntity>? {
            val attachmentsJson =
                cursor.getBlob(column)
            return if (attachmentsJson.nonNullNoEmpty()) {
                val attachmentsEntity =
                    MsgPack.decodeFromByteArrayEx(
                        ListSerializer(DboEntity.serializer()),
                        attachmentsJson
                    )
                attachmentsEntity.ifEmpty {
                    null
                }
            } else {
                null
            }
        }

        fun mapAttachmentsFromBlob(
            cursor: Cursor,
            attachmentFor: AttachmentFor,
        ): List<DboEntity>? {
            return mapAttachmentsFromBlobWithColumn(cursor, dataColumnFor(attachmentFor))
        }

        fun putAttachmentsToBlob(cv: ContentValues, column: String, dbos: List<DboEntity>?) {
            if (dbos.nonNullNoEmpty()) {
                cv.put(
                    column,
                    MsgPack.encodeToByteArrayEx(
                        ListSerializer(DboEntity.serializer()),
                        dbos
                    )
                )
            } else {
                cv.putNull(column)
            }
        }

        fun putAttachmentsToBlob(
            cv: ContentValues,
            attachmentFor: AttachmentFor,
            dbos: List<DboEntity>?
        ) {
            putAttachmentsToBlob(cv, dataColumnFor(attachmentFor), dbos)
        }

        internal fun elementIdForType(attachmentFor: AttachmentFor): String {
            return when (attachmentFor) {
                AttachmentFor.COMMENT -> CommentsColumns.COMMENT_ID
                AttachmentFor.MESSAGE -> MessagesColumns.CONVERSATION_MESSAGE_ID
                AttachmentFor.POST -> PostsColumns.POST_ID
            }
        }

        internal fun elementOwnerIdForType(attachmentFor: AttachmentFor): String {
            return when (attachmentFor) {
                AttachmentFor.COMMENT -> CommentsColumns.SOURCE_ID
                AttachmentFor.MESSAGE -> MessagesColumns.PEER_ID
                AttachmentFor.POST -> PostsColumns.OWNER_ID
            }
        }

        internal fun uriForType(attachmentFor: AttachmentFor, accountId: Long): Uri {
            return when (attachmentFor) {
                AttachmentFor.COMMENT -> getCommentsContentUriFor(accountId)
                AttachmentFor.MESSAGE -> getMessageContentUriFor(accountId)
                AttachmentFor.POST -> getPostsContentUriFor(accountId)
            }
        }

        internal fun dataColumnFor(attachmentFor: AttachmentFor): String {
            return when (attachmentFor) {
                AttachmentFor.COMMENT -> CommentsColumns.ATTACHMENTS_BLOB
                AttachmentFor.MESSAGE -> MessagesColumns.ATTACHMENTS_BLOB
                AttachmentFor.POST -> PostsColumns.ATTACHMENTS_BLOB
            }
        }
    }
}