package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import com.google.gson.reflect.TypeToken
import dev.ragnarok.fenrir.db.AttachToType
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getCommentsContentUriFor
import dev.ragnarok.fenrir.db.column.CommentsColumns
import dev.ragnarok.fenrir.db.impl.AttachmentsStorage.Companion.appendAttachOperationWithBackReference
import dev.ragnarok.fenrir.db.interfaces.Cancelable
import dev.ragnarok.fenrir.db.interfaces.ICommentsStorage
import dev.ragnarok.fenrir.db.model.entity.CommentEntity
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities
import dev.ragnarok.fenrir.exception.DatabaseException
import dev.ragnarok.fenrir.model.CommentUpdate
import dev.ragnarok.fenrir.model.Commented
import dev.ragnarok.fenrir.model.DraftComment
import dev.ragnarok.fenrir.model.criteria.CommentsCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Exestime.log
import dev.ragnarok.fenrir.util.Unixtime.now
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.subjects.PublishSubject

internal class CommentsStorage(base: AppStorages) : AbsStorage(base), ICommentsStorage {
    private val minorUpdatesPublisher: PublishSubject<CommentUpdate> = PublishSubject.create()
    private val mStoreLock = Any()
    override fun insert(
        accountId: Int,
        sourceId: Int,
        sourceOwnerId: Int,
        sourceType: Int,
        dbos: List<CommentEntity>,
        owners: OwnerEntities?,
        clearBefore: Boolean
    ): Single<IntArray> {
        return Single.create { emitter: SingleEmitter<IntArray> ->
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBefore) {
                val delete = ContentProviderOperation
                    .newDelete(getCommentsContentUriFor(accountId))
                    .withSelection(
                        CommentsColumns.SOURCE_ID + " = ? " +
                                " AND " + CommentsColumns.SOURCE_OWNER_ID + " = ? " +
                                " AND " + CommentsColumns.COMMENT_ID + " != ? " +
                                " AND " + CommentsColumns.SOURCE_TYPE + " = ?", arrayOf(
                            sourceId.toString(),
                            sourceOwnerId.toString(),
                            java.lang.String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                            sourceType.toString()
                        )
                    ).build()
                operations.add(delete)
            }
            val indexes = IntArray(dbos.size)
            for (i in dbos.indices) {
                val dbo = dbos[i]
                val mainPostHeaderOperation = ContentProviderOperation
                    .newInsert(getCommentsContentUriFor(accountId))
                    .withValues(getCV(sourceId, sourceOwnerId, sourceType, dbo))
                    .build()
                val mainPostHeaderIndex =
                    addToListAndReturnIndex(operations, mainPostHeaderOperation)
                indexes[i] = mainPostHeaderIndex
                dbo.attachments.nonNullNoEmpty {
                    for (attachmentEntity in it) {
                        appendAttachOperationWithBackReference(
                            operations,
                            accountId,
                            AttachToType.COMMENT,
                            mainPostHeaderIndex,
                            attachmentEntity
                        )
                    }
                }
            }
            if (owners != null) {
                OwnersStorage.appendOwnersInsertOperations(operations, accountId, owners)
            }
            var results: Array<ContentProviderResult>
            synchronized(mStoreLock) {
                results = context.contentResolver.applyBatch(
                    MessengerContentProvider.AUTHORITY,
                    operations
                )
            }
            val ids = IntArray(dbos.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            emitter.onSuccess(ids)
        }
    }

    private fun createCursorByCriteria(criteria: CommentsCriteria): Cursor? {
        val uri = getCommentsContentUriFor(criteria.accountId)
        val range = criteria.getRange()
        val commented = criteria.commented
        return if (range == null) {
            contentResolver.query(
                uri, null,
                CommentsColumns.SOURCE_ID + " = ? AND " +
                        CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                        CommentsColumns.SOURCE_TYPE + " = ? AND " +
                        CommentsColumns.COMMENT_ID + " != ?", arrayOf(
                    commented.sourceId.toString(),
                    commented.sourceOwnerId.toString(),
                    commented.sourceType.toString(),
                    java.lang.String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID)
                ),
                CommentsColumns.COMMENT_ID + " DESC"
            )
        } else {
            contentResolver.query(
                uri,
                null,
                BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?",
                arrayOf(range.first.toString(), range.last.toString()),
                CommentsColumns.COMMENT_ID + " DESC"
            )
        }
    }

    override fun getDbosByCriteria(criteria: CommentsCriteria): Single<List<CommentEntity>> {
        return Single.create { emitter: SingleEmitter<List<CommentEntity>> ->
            val cursor = createCursorByCriteria(criteria)
            val cancelation = object : Cancelable {
                override val isOperationCancelled: Boolean
                    get() = emitter.isDisposed
            }
            val dbos: MutableList<CommentEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed) {
                        break
                    }
                    dbos.add(
                        mapDbo(
                            criteria.accountId, cursor,
                            includeAttachments = true,
                            forceAttachments = false,
                            cancelable = cancelation
                        )
                    )
                }
                cursor.close()
            }
            emitter.onSuccess(dbos)
        }
    }

    override fun findEditingComment(accountId: Int, commented: Commented): Maybe<DraftComment>? {
        return Maybe.create { e: MaybeEmitter<DraftComment> ->
            val cursor = contentResolver.query(
                getCommentsContentUriFor(accountId), null,
                CommentsColumns.COMMENT_ID + " = ? AND " +
                        CommentsColumns.SOURCE_ID + " = ? AND " +
                        CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                        CommentsColumns.SOURCE_TYPE + " = ?", arrayOf(
                    java.lang.String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                    commented.sourceId.toString(),
                    commented.sourceOwnerId.toString(),
                    commented.sourceType.toString()
                ), null
            )
            var comment: DraftComment? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    val dbid = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    val body = cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.TEXT))
                    comment = DraftComment(dbid).setBody(body)
                }
                cursor.close()
            }
            if (comment != null) {
                e.onSuccess(comment)
            }
            e.onComplete()
        }.flatMap { comment: DraftComment ->
            stores
                .attachments()
                .getCount(accountId, AttachToType.COMMENT, comment.id)
                .flatMapMaybe {
                    Maybe.just(
                        comment.setAttachmentsCount(
                            it
                        )
                    )
                }
        }
    }

    override fun saveDraftComment(
        accountId: Int,
        commented: Commented,
        text: String?,
        replyToUser: Int,
        replyToComment: Int
    ): Single<Int> {
        return Single.create { e: SingleEmitter<Int> ->
            val start = System.currentTimeMillis()
            var id = findEditingCommentId(accountId, commented)
            val contentValues = ContentValues()
            contentValues.put(CommentsColumns.COMMENT_ID, CommentsColumns.PROCESSING_COMMENT_ID)
            contentValues.put(CommentsColumns.TEXT, text)
            contentValues.put(CommentsColumns.SOURCE_ID, commented.sourceId)
            contentValues.put(CommentsColumns.SOURCE_OWNER_ID, commented.sourceOwnerId)
            contentValues.put(CommentsColumns.SOURCE_TYPE, commented.sourceType)
            contentValues.put(CommentsColumns.FROM_ID, accountId)
            contentValues.put(CommentsColumns.DATE, now())
            contentValues.put(CommentsColumns.REPLY_TO_USER, replyToUser)
            contentValues.put(CommentsColumns.REPLY_TO_COMMENT, replyToComment)
            contentValues.put(CommentsColumns.THREADS_COUNT, 0)
            contentValues.putNull(CommentsColumns.THREADS)
            contentValues.put(CommentsColumns.LIKES, 0)
            contentValues.put(CommentsColumns.USER_LIKES, 0)
            val commentsWithAccountUri = getCommentsContentUriFor(accountId)
            if (id == null) {
                val uri = contentResolver.insert(commentsWithAccountUri, contentValues)
                if (uri == null) {
                    e.onError(DatabaseException("Result URI is null"))
                    return@create
                }
                id = uri.pathSegments[1].toInt()
            } else {
                contentResolver.update(
                    commentsWithAccountUri, contentValues,
                    BaseColumns._ID + " = ?", arrayOf(id.toString())
                )
            }
            e.onSuccess(id)
            log("CommentsStorage.saveDraftComment", start, "id: $id")
        }
    }

    override fun commitMinorUpdate(update: CommentUpdate): Completable {
        return Completable.fromAction {
            val cv = ContentValues()
            if (update.hasLikesUpdate()) {
                cv.put(CommentsColumns.USER_LIKES, update.likeUpdate.isUserLikes)
                cv.put(CommentsColumns.LIKES, update.likeUpdate.count)
            }
            if (update.hasDeleteUpdate()) {
                cv.put(CommentsColumns.DELETED, update.deleteUpdate.isDeleted)
            }
            val uri = getCommentsContentUriFor(update.accountId)
            val where =
                CommentsColumns.SOURCE_OWNER_ID + " = ? AND " + CommentsColumns.COMMENT_ID + " = ?"
            val args =
                arrayOf(update.commented.sourceOwnerId.toString(), update.commentId.toString())
            contentResolver.update(uri, cv, where, args)
            minorUpdatesPublisher.onNext(update)
        }
    }

    override fun observeMinorUpdates(): Observable<CommentUpdate> {
        return minorUpdatesPublisher
    }

    override fun deleteByDbid(accountId: Int, dbid: Int): Completable {
        return Completable.fromAction {
            val uri = getCommentsContentUriFor(accountId)
            val where = BaseColumns._ID + " = ?"
            val args = arrayOf(dbid.toString())
            contentResolver.delete(uri, where, args)
        }
    }

    private fun findEditingCommentId(aid: Int, commented: Commented): Int? {
        val projection = arrayOf(BaseColumns._ID)
        val cursor = contentResolver.query(
            getCommentsContentUriFor(aid), projection,
            CommentsColumns.COMMENT_ID + " = ? AND " +
                    CommentsColumns.SOURCE_ID + " = ? AND " +
                    CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                    CommentsColumns.SOURCE_TYPE + " = ?", arrayOf(
                CommentsColumns.PROCESSING_COMMENT_ID.toString(),
                commented.sourceId.toString(),
                commented.sourceOwnerId.toString(),
                commented.sourceType.toString()
            ), null
        )
        var result: Int? = null
        if (cursor != null) {
            if (cursor.moveToNext()) {
                result = cursor.getInt(0)
            }
            cursor.close()
        }
        return result
    }

    private fun mapDbo(
        accountId: Int,
        cursor: Cursor,
        includeAttachments: Boolean,
        forceAttachments: Boolean,
        cancelable: Cancelable
    ): CommentEntity {
        val attachmentsCount =
            cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.ATTACHMENTS_COUNT))
        val dbid = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        val sourceId = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_ID))
        val sourceOwnerId =
            cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_OWNER_ID))
        val sourceType = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_TYPE))
        val sourceAccessKey =
            cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_ACCESS_KEY))
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.COMMENT_ID))
        val threadsJson = cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.THREADS))
        val dbo = CommentEntity().set(sourceId, sourceOwnerId, sourceType, sourceAccessKey, id)
            .setFromId(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.FROM_ID)))
            .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(CommentsColumns.DATE)))
            .setText(cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.TEXT)))
            .setReplyToUserId(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.REPLY_TO_USER)))
            .setThreadsCount(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.THREADS_COUNT)))
            .setReplyToComment(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.REPLY_TO_COMMENT)))
            .setLikesCount(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.LIKES)))
            .setUserLikes(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.USER_LIKES)) == 1)
            .setCanLike(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.CAN_LIKE)) == 1)
            .setCanEdit(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.CAN_EDIT)) == 1)
            .setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.DELETED)) == 1)
        if (threadsJson != null) {
            dbo.threads =
                GSON.fromJson(threadsJson, THREADS_TYPE)
        }
        if (includeAttachments && (attachmentsCount > 0 || forceAttachments)) {
            dbo.attachments = stores
                .attachments()
                .getAttachmentsDbosSync(accountId, AttachToType.COMMENT, dbid, cancelable)
        }
        return dbo
    }

    companion object {
        private val THREADS_TYPE = object : TypeToken<List<CommentEntity>>() {}.type
        fun getCV(
            sourceId: Int,
            sourceOwnerId: Int,
            sourceType: Int,
            dbo: CommentEntity
        ): ContentValues {
            val cv = ContentValues()
            cv.put(CommentsColumns.COMMENT_ID, dbo.id)
            cv.put(CommentsColumns.FROM_ID, dbo.fromId)
            cv.put(CommentsColumns.DATE, dbo.date)
            cv.put(CommentsColumns.TEXT, dbo.text)
            cv.put(CommentsColumns.REPLY_TO_USER, dbo.replyToUserId)
            cv.put(CommentsColumns.REPLY_TO_COMMENT, dbo.replyToComment)
            cv.put(CommentsColumns.THREADS_COUNT, dbo.threadsCount)
            if (dbo.threads.nonNullNoEmpty()) {
                cv.put(CommentsColumns.THREADS, GSON.toJson(dbo.threads))
            } else {
                cv.putNull(CommentsColumns.THREADS)
            }
            cv.put(CommentsColumns.LIKES, dbo.likesCount)
            cv.put(CommentsColumns.USER_LIKES, dbo.isUserLikes)
            cv.put(CommentsColumns.CAN_LIKE, dbo.isCanLike)
            cv.put(CommentsColumns.ATTACHMENTS_COUNT, dbo.attachmentsCount)
            cv.put(CommentsColumns.SOURCE_ID, sourceId)
            cv.put(CommentsColumns.SOURCE_OWNER_ID, sourceOwnerId)
            cv.put(CommentsColumns.SOURCE_TYPE, sourceType)
            cv.put(CommentsColumns.DELETED, dbo.isDeleted)
            return cv
        }
    }

}