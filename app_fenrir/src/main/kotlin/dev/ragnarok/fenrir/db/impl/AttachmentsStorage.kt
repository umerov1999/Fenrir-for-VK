package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.AttachToType
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
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.msgpack.MsgPack

internal class AttachmentsStorage(base: AppStorages) : AbsStorage(base), IAttachmentsStorage {
    override fun getAttachmentsFromBlob(
        accountId: Long,
        @AttachToType attachToType: Int,
        where: String,
        args: Array<String>?
    ): Flow<List<DboEntity>?> {
        return flow {
            val uri = uriForType(attachToType, accountId)
            val cursor = contentResolver.query(
                uri,
                arrayOf(dataColumnFor(attachToType)),
                where,
                args,
                null
            )
            var ret: List<DboEntity>? = null
            if (isActive()) {
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        ret = mapAttachmentsFromBlob(cursor, attachToType)
                    }
                    cursor.close()
                }
            }
            emit(ret)
        }
    }

    override fun getAttachmentsFromBlob(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int
    ): Flow<List<DboEntity>?> {
        return getAttachmentsFromBlob(
            accountId,
            attachToType,
            BaseColumns._ID + " = ?",
            arrayOf(attachToDbId.toString())
        )
    }

    override fun remove(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachmentKey: String,
        where: String,
        args: Array<String>?
    ): Flow<Boolean> {
        return flow {
            val uri = uriForType(attachToType, accountId)
            val cursor = contentResolver.query(
                uri,
                arrayOf(dataColumnFor(attachToType)),
                where,
                args,
                null
            )
            var result: List<DboEntity>? = null
            if (isActive()) {
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        result = mapAttachmentsFromBlob(cursor, attachToType)
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
                attachToType = attachToType,
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
        @AttachToType attachToType: Int,
        attachmentKey: String,
        attachToDbId: Int
    ): Flow<Boolean> {
        return remove(
            accountId = accountId,
            attachToType = attachToType,
            attachmentKey = attachmentKey,
            where = BaseColumns._ID + " = ?",
            args = arrayOf(attachToDbId.toString())
        )
    }

    override fun removeAll(
        accountId: Long,
        @AttachToType attachToType: Int,
        where: String,
        args: Array<String>?
    ): Flow<Boolean> {
        return flow {
            val cv = ContentValues()
            cv.putNull(dataColumnFor(attachToType))
            val operations = ArrayList<ContentProviderOperation>()
            operations.add(
                ContentProviderOperation.newUpdate(uriForType(attachToType, accountId))
                    .withSelection(where, args).build()
            )
            contentResolver.applyBatch(FenrirContentProvider.AUTHORITY, operations)
            emit(true)
        }
    }

    override fun removeAll(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int
    ): Flow<Boolean> {
        return removeAll(
            accountId,
            attachToType,
            BaseColumns._ID + " = ?",
            arrayOf(attachToDbId.toString())
        )
    }

    override fun getAttachmentsCount(
        accountId: Long,
        @AttachToType attachToType: Int,
        where: String,
        args: Array<String>?
    ): Flow<Int> {
        return flow {
            val uri = uriForType(attachToType, accountId)
            val cursor = contentResolver.query(
                uri,
                arrayOf(dataColumnFor(attachToType)),
                where,
                args,
                null
            )
            var count = 0
            if (isActive()) {
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        count = mapAttachmentsFromBlob(cursor, attachToType)?.size.orZero()
                    }
                    cursor.close()
                }
            }
            emit(count)
        }
    }

    override fun getAttachmentsCount(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int
    ): Flow<Int> {
        return getAttachmentsCount(
            accountId,
            attachToType,
            BaseColumns._ID + " = ?",
            arrayOf(attachToDbId.toString())
        )
    }

    override fun attachToBlobSync(
        cv: ContentValues?,
        @AttachToType attachToType: Int,
        attachmentsEntities: List<DboEntity>?,
        where: String?,
        args: Array<String>?,
        replace: Boolean,
        accountId: Long?
    ): Flow<Boolean> {
        return flow {
            val tmpAttachmentsEntities = ArrayList(attachmentsEntities.orEmpty())
            if (!replace && where.nonNullNoEmpty() && accountId != null) {
                val uri = uriForType(attachToType, accountId)
                val cursor = contentResolver.query(
                    uri,
                    arrayOf(dataColumnFor(attachToType)),
                    where,
                    args,
                    null
                )
                if (isActive()) {
                    if (cursor != null) {
                        if (cursor.moveToNext()) {
                            val attachmentsEntity = mapAttachmentsFromBlob(cursor, attachToType)
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

            putAttachmentsToBlob(tmpCv, attachToType, tmpAttachmentsEntities)

            if (cv == null && where.nonNullNoEmpty() && accountId != null) {
                val operations = ArrayList<ContentProviderOperation>()
                operations.add(
                    ContentProviderOperation.newUpdate(uriForType(attachToType, accountId))
                        .withSelection(where, args).withValues(tmpCv).build()
                )
                contentResolver.applyBatch(FenrirContentProvider.AUTHORITY, operations)
            }
            emit(true)
        }
    }

    override fun attachToBlobSync(
        cv: ContentValues?,
        @AttachToType attachToType: Int,
        attachmentsEntities: List<DboEntity>?,
        attachToDbId: Int?,
        replace: Boolean,
        accountId: Long?
    ): Flow<Boolean> {
        return if (attachToDbId == null) {
            attachToBlobSync(
                cv = cv,
                attachToType = attachToType,
                attachmentsEntities = attachmentsEntities,
                where = null,
                args = null,
                replace = replace,
                accountId = accountId
            )
        } else {
            attachToBlobSync(
                cv = cv,
                attachToType = attachToType,
                attachmentsEntities = attachmentsEntities,
                where = BaseColumns._ID + " = ?",
                args = arrayOf(attachToDbId.toString()),
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
            @AttachToType attachToType: Int
        ): List<DboEntity>? {
            return mapAttachmentsFromBlobWithColumn(cursor, dataColumnFor(attachToType))
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
            @AttachToType attachToType: Int,
            dbos: List<DboEntity>?
        ) {
            putAttachmentsToBlob(cv, dataColumnFor(attachToType), dbos)
        }

        internal fun uriForType(@AttachToType type: Int, accountId: Long): Uri {
            return when (type) {
                AttachToType.COMMENT -> getCommentsContentUriFor(accountId)
                AttachToType.MESSAGE -> getMessageContentUriFor(accountId)
                AttachToType.POST -> getPostsContentUriFor(accountId)
                else -> throw IllegalArgumentException()
            }
        }

        internal fun dataColumnFor(@AttachToType type: Int): String {
            return when (type) {
                AttachToType.COMMENT -> CommentsColumns.ATTACHMENTS_BLOB
                AttachToType.MESSAGE -> MessagesColumns.ATTACHMENTS_BLOB
                AttachToType.POST -> PostsColumns.ATTACHMENTS_BLOB
                else -> throw IllegalArgumentException()
            }
        }
    }
}