package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getNotificationsContentUriFor
import dev.ragnarok.fenrir.db.column.NotificationColumns
import dev.ragnarok.fenrir.db.interfaces.IFeedbackStorage
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities
import dev.ragnarok.fenrir.db.model.entity.feedback.*
import dev.ragnarok.fenrir.model.criteria.NotificationsCriteria
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class FeedbackStorage(context: AppStorages) : AbsStorage(context), IFeedbackStorage {
    companion object {
        private const val LIKE = 1
        private const val LIKE_COMMENT = 2
        private const val COPY = 3
        private const val MENTION = 4
        private const val MENTION_COMMENT = 5
        private const val WALL_PUBLISH = 6
        private const val NEW_COMMENT = 7
        private const val REPLY_COMMENT = 8
        private const val USERS = 9
        private val TYPES: MutableMap<Class<*>, Int> = HashMap(8)
        private fun typeForClass(c: Class<out FeedbackEntity>): Int {
            return TYPES[c]
                ?: throw UnsupportedOperationException("Unsupported type: $c")
        }

        private fun classForType(dbtype: Int): Class<out FeedbackEntity> {
            when (dbtype) {
                LIKE -> return LikeEntity::class.java
                LIKE_COMMENT -> return LikeCommentEntity::class.java
                COPY -> return CopyEntity::class.java
                MENTION -> return MentionEntity::class.java
                MENTION_COMMENT -> return MentionCommentEntity::class.java
                WALL_PUBLISH -> return PostFeedbackEntity::class.java
                NEW_COMMENT -> return NewCommentEntity::class.java
                REPLY_COMMENT -> return ReplyCommentEntity::class.java
                USERS -> return UsersEntity::class.java
            }
            throw UnsupportedOperationException("Unsupported type: $dbtype")
        }

        init {
            TYPES[LikeEntity::class.java] = LIKE
            TYPES[LikeCommentEntity::class.java] = LIKE_COMMENT
            TYPES[CopyEntity::class.java] = COPY
            TYPES[MentionEntity::class.java] = MENTION
            TYPES[MentionCommentEntity::class.java] = MENTION_COMMENT
            TYPES[PostFeedbackEntity::class.java] = WALL_PUBLISH
            TYPES[NewCommentEntity::class.java] = NEW_COMMENT
            TYPES[ReplyCommentEntity::class.java] =
                REPLY_COMMENT
            TYPES[UsersEntity::class.java] = USERS
        }
    }

    override fun insert(
        accountId: Int,
        dbos: List<FeedbackEntity>,
        owners: OwnerEntities?,
        clearBefore: Boolean
    ): Single<IntArray> {
        return Single.create { emitter: SingleEmitter<IntArray> ->
            val uri = getNotificationsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBefore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            val indexes = IntArray(dbos.size)
            for (i in dbos.indices) {
                val dbo = dbos[i]
                val cv = ContentValues()
                cv.put(NotificationColumns.DATE, dbo.date)
                cv.put(NotificationColumns.TYPE, typeForClass(dbo.javaClass))
                cv.put(NotificationColumns.DATA, GSON.toJson(dbo))
                val index = addToListAndReturnIndex(
                    operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
                indexes[i] = index
            }
            OwnersStorage.appendOwnersInsertOperations(operations, accountId, owners)
            val results = contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            val ids = IntArray(dbos.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            emitter.onSuccess(ids)
        }
    }

    override fun findByCriteria(criteria: NotificationsCriteria): Single<List<FeedbackEntity>> {
        return Single.create { e: SingleEmitter<List<FeedbackEntity>> ->
            val range = criteria.range
            val uri = getNotificationsContentUriFor(criteria.accountId)
            val cursor: Cursor? = if (range != null) {
                val where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?"
                val args = arrayOf(range.first.toString(), range.last.toString())
                context.contentResolver.query(
                    uri,
                    null,
                    where,
                    args,
                    NotificationColumns.DATE + " DESC"
                )
            } else {
                context.contentResolver.query(
                    uri,
                    null,
                    null,
                    null,
                    NotificationColumns.DATE + " DESC"
                )
            }
            val dtos: MutableList<FeedbackEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    val dto = mapDto(cursor)
                    dtos.add(dto)
                }
                cursor.close()
            }
            e.onSuccess(dtos)
        }
    }

    private fun mapDto(cursor: Cursor): FeedbackEntity {
        val dbtype = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationColumns.TYPE))
        val data = cursor.getString(cursor.getColumnIndexOrThrow(NotificationColumns.DATA))
        val feedbackClass = classForType(dbtype)
        return GSON.fromJson(data, feedbackClass)
    }
}