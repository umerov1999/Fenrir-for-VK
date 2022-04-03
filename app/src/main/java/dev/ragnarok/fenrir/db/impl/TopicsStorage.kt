package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getGroupsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getTopicsContentUriFor
import dev.ragnarok.fenrir.db.column.GroupColumns
import dev.ragnarok.fenrir.db.column.TopicsColumns
import dev.ragnarok.fenrir.db.impl.OwnersStorage.Companion.appendOwnersInsertOperations
import dev.ragnarok.fenrir.db.interfaces.ITopicsStore
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities
import dev.ragnarok.fenrir.db.model.entity.PollEntity
import dev.ragnarok.fenrir.db.model.entity.TopicEntity
import dev.ragnarok.fenrir.model.criteria.TopicsCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import kotlin.math.abs

internal class TopicsStorage(base: AppStorages) : AbsStorage(base), ITopicsStore {
    override fun getByCriteria(criteria: TopicsCriteria): Single<List<TopicEntity>> {
        return Single.create { e: SingleEmitter<List<TopicEntity>> ->
            val uri = getTopicsContentUriFor(criteria.accountId)
            val where: String
            val args: Array<String>
            if (criteria.range != null) {
                val range = criteria.range
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?"
                args = arrayOf(range.first.toString(), range.last.toString())
            } else {
                where = TopicsColumns.OWNER_ID + " = ?"
                args = arrayOf(criteria.ownerId.toString())
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val topics = ArrayList<TopicEntity>(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    topics.add(mapDbo(cursor))
                }
                cursor.close()
            }
            e.onSuccess(topics)
        }
    }

    override fun store(
        accountId: Int,
        ownerId: Int,
        topics: List<TopicEntity>,
        owners: OwnerEntities?,
        canAddTopic: Boolean,
        defaultOrder: Int,
        clearBefore: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val operations = ArrayList<ContentProviderOperation>()
            val uri = getTopicsContentUriFor(accountId)
            if (owners != null) {
                appendOwnersInsertOperations(operations, accountId, owners)
            }
            if (clearBefore) {
                operations.add(
                    ContentProviderOperation.newDelete(uri)
                        .withSelection(TopicsColumns.OWNER_ID + " = ?", arrayOf(ownerId.toString()))
                        .build()
                )
            }
            for (dbo in topics) {
                operations.add(
                    ContentProviderOperation.newInsert(uri)
                        .withValues(getCV(dbo))
                        .build()
                )
            }
            val cv = ContentValues()
            cv.put(GroupColumns.CAN_ADD_TOPICS, canAddTopic)
            cv.put(GroupColumns.TOPICS_ORDER, defaultOrder)
            operations.add(
                ContentProviderOperation
                    .newUpdate(getGroupsContentUriFor(accountId))
                    .withValues(cv)
                    .withSelection(BaseColumns._ID + " = ?", arrayOf(abs(ownerId).toString()))
                    .build()
            )
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
        }
    }

    override fun attachPoll(
        accountId: Int,
        ownerId: Int,
        topicId: Int,
        pollDbo: PollEntity?
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val cv = ContentValues()
            cv.put(TopicsColumns.ATTACHED_POLL, if (pollDbo != null) GSON.toJson(pollDbo) else null)
            val uri = getTopicsContentUriFor(accountId)
            val where = TopicsColumns.TOPIC_ID + " = ? AND " + TopicsColumns.OWNER_ID + " = ?"
            val args = arrayOf(topicId.toString(), topicId.toString())
            contentResolver.update(uri, cv, where, args)
            e.onComplete()
        }
    }

    companion object {
        fun getCV(dbo: TopicEntity): ContentValues {
            val cv = ContentValues()
            cv.put(TopicsColumns.TOPIC_ID, dbo.id)
            cv.put(TopicsColumns.OWNER_ID, dbo.ownerId)
            cv.put(TopicsColumns.TITLE, dbo.title)
            cv.put(TopicsColumns.CREATED, dbo.createdTime)
            cv.put(TopicsColumns.CREATED_BY, dbo.creatorId)
            cv.put(TopicsColumns.UPDATED, dbo.lastUpdateTime)
            cv.put(TopicsColumns.UPDATED_BY, dbo.updatedBy)
            cv.put(TopicsColumns.IS_CLOSED, dbo.isClosed)
            cv.put(TopicsColumns.IS_FIXED, dbo.isFixed)
            cv.put(TopicsColumns.COMMENTS, dbo.commentsCount)
            cv.put(TopicsColumns.FIRST_COMMENT, dbo.firstComment)
            cv.put(TopicsColumns.LAST_COMMENT, dbo.lastComment)
            cv.put(
                TopicsColumns.ATTACHED_POLL,
                if (dbo.poll != null) GSON.toJson(dbo.poll) else null
            )
            return cv
        }

        private fun mapDbo(cursor: Cursor): TopicEntity {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(TopicsColumns.TOPIC_ID))
            val ownerId = cursor.getInt(cursor.getColumnIndexOrThrow(TopicsColumns.OWNER_ID))
            val dbo = TopicEntity().set(id, ownerId)
                .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TopicsColumns.TITLE)))
                .setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(TopicsColumns.CREATED)))
                .setCreatorId(cursor.getInt(cursor.getColumnIndexOrThrow(TopicsColumns.CREATED_BY)))
                .setLastUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow(TopicsColumns.UPDATED)))
                .setUpdatedBy(cursor.getInt(cursor.getColumnIndexOrThrow(TopicsColumns.UPDATED_BY)))
                .setClosed(cursor.getInt(cursor.getColumnIndexOrThrow(TopicsColumns.IS_CLOSED)) == 1)
                .setFixed(cursor.getInt(cursor.getColumnIndexOrThrow(TopicsColumns.IS_FIXED)) == 1)
                .setCommentsCount(cursor.getInt(cursor.getColumnIndexOrThrow(TopicsColumns.COMMENTS)))
                .setFirstComment(cursor.getString(cursor.getColumnIndexOrThrow(TopicsColumns.FIRST_COMMENT)))
                .setLastComment(cursor.getString(cursor.getColumnIndexOrThrow(TopicsColumns.LAST_COMMENT)))
            val pollJson =
                cursor.getString(cursor.getColumnIndexOrThrow(TopicsColumns.ATTACHED_POLL))
            if (pollJson.nonNullNoEmpty()) {
                dbo.poll = GSON.fromJson(pollJson, PollEntity::class.java)
            }
            return dbo
        }
    }
}