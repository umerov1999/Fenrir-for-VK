package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFeedListsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getNewsContentUriFor
import dev.ragnarok.fenrir.db.column.FeedListsColumns
import dev.ragnarok.fenrir.db.column.FeedListsColumns.getCV
import dev.ragnarok.fenrir.db.column.NewsColumns
import dev.ragnarok.fenrir.db.interfaces.IFeedStorage
import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.model.FeedSourceCriteria
import dev.ragnarok.fenrir.model.criteria.FeedCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.join
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class FeedStorage(base: AppStorages) : AbsStorage(base), IFeedStorage {
    private val storeLock = Any()
    override fun findByCriteria(criteria: FeedCriteria): Single<List<NewsEntity>> {
        return Single.create { e: SingleEmitter<List<NewsEntity>> ->
            val uri = getNewsContentUriFor(criteria.accountId)
            val data: MutableList<NewsEntity> = ArrayList()
            synchronized(storeLock) {
                val cursor: Cursor? = if (criteria.range != null) {
                    val range = criteria.range
                    context.contentResolver.query(
                        uri,
                        null,
                        BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?",
                        arrayOf(range.first.toString(), range.last.toString()),
                        null
                    )
                } else {
                    context.contentResolver.query(uri, null, null, null, null)
                }
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        if (e.isDisposed) {
                            break
                        }
                        data.add(mapNewsBase(cursor))
                    }
                    cursor.close()
                }
            }
            e.onSuccess(data)
        }
    }

    override fun store(
        accountId: Int,
        data: List<NewsEntity>,
        owners: OwnerEntities?,
        clearBeforeStore: Boolean
    ): Single<IntArray> {
        return Single.create { emitter: SingleEmitter<IntArray> ->
            val uri = getNewsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeStore) {
                // for performance test (before - 500-600ms, after - 200-300ms)
                //operations.add(ContentProviderOperation.newDelete(MessengerContentProvider.getNewsAttachmentsContentUriFor(accountId))
                //        .build());
                operations.add(ContentProviderOperation.newDelete(uri).build())
            }
            val indexes = IntArray(data.size)
            for (i in data.indices) {
                val dbo = data[i]
                val cv = getCV(dbo)
                val mainPostHeaderOperation = ContentProviderOperation
                    .newInsert(uri)
                    .withValues(cv)
                    .build()
                val mainPostHeaderIndex =
                    addToListAndReturnIndex(operations, mainPostHeaderOperation)
                indexes[i] = mainPostHeaderIndex
            }
            if (owners != null) {
                OwnersStorage.appendOwnersInsertOperations(operations, accountId, owners)
            }
            var results: Array<ContentProviderResult>
            synchronized(storeLock) {
                results = context.contentResolver.applyBatch(
                    MessengerContentProvider.AUTHORITY,
                    operations
                )
            }
            val ids = IntArray(data.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            emitter.onSuccess(ids)
        }
    }

    override fun storeLists(accountid: Int, entities: List<FeedListEntity>): Completable {
        return Completable.create { e: CompletableEmitter ->
            val uri = getFeedListsContentUriFor(accountid)
            val operations = ArrayList<ContentProviderOperation>()
            operations.add(
                ContentProviderOperation.newDelete(uri)
                    .build()
            )
            for (entity in entities) {
                operations.add(
                    ContentProviderOperation.newInsert(uri)
                        .withValues(getCV(entity))
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
        }
    }

    override fun getAllLists(criteria: FeedSourceCriteria): Single<List<FeedListEntity>> {
        return Single.create { e: SingleEmitter<List<FeedListEntity>> ->
            val uri = getFeedListsContentUriFor(criteria.accountId)
            val cursor = contentResolver.query(uri, null, null, null, null)
            val data: MutableList<FeedListEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    data.add(mapList(cursor))
                }
                cursor.close()
            }
            e.onSuccess(data)
        }
    }

    /*private void fillAttachmentsOperations(int accountId, @NonNull VKApiAttachment attachment, @NonNull List<ContentProviderOperation> target,
                                           int parentPostHeaderOperationIndex) {
        Logger.d("fillAttachmentsOperations", "attachment: " + attachment.toAttachmentString());

        ContentValues cv = new ContentValues();
        cv.put(NewsAttachmentsColumns.TYPE, Types.from(attachment.getType()));
        cv.put(NewsAttachmentsColumns.DATA, serializeAttachment(attachment));

        target.add(ContentProviderOperation.newInsert(MessengerContentProvider.getNewsAttachmentsContentUriFor(accountId))
                .withValues(cv)
                .withValueBackReference(NewsAttachmentsColumns.N_ID, parentPostHeaderOperationIndex)
                .build());
    }*/
    private fun mapNewsBase(cursor: Cursor): NewsEntity {
        val friendString = cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.TAG_FRIENDS))
        val dbo = NewsEntity()
        if (friendString.nonNullNoEmpty()) {
            val strArray = friendString.split(",".toRegex()).toTypedArray()
            val intArray = arrayOfNulls<Int>(strArray.size)
            for (i in strArray.indices) {
                intArray[i] = strArray[i].toInt()
            }
            dbo.friendsTags = listOf(*intArray)
        } else {
            dbo.friendsTags = null
        }
        dbo.setType(cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.TYPE)))
            .setSourceId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.SOURCE_ID)))
            .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(NewsColumns.DATE)))
            .setPostId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.POST_ID)))
            .setPostType(cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.POST_TYPE)))
            .setFinalPost(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.FINAL_POST)) == 1)
            .setCopyOwnerId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COPY_OWNER_ID)))
            .setCopyPostId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COPY_POST_ID)))
            .setCopyPostDate(cursor.getLong(cursor.getColumnIndexOrThrow(NewsColumns.COPY_POST_DATE)))
            .setText(cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.TEXT)))
            .setCanEdit(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_EDIT)) == 1)
            .setCanDelete(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_DELETE)) == 1)
            .setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COMMENT_COUNT)))
            .setCanPostComment(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COMMENT_CAN_POST)) == 1)
            .setLikesCount(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.LIKE_COUNT)))
            .setUserLikes(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.USER_LIKE)) == 1)
            .setCanLike(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_LIKE)) == 1)
            .setCanPublish(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_PUBLISH)) == 1)
            .setRepostCount(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.REPOSTS_COUNT)))
            .setUserReposted(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.USER_REPOSTED)) == 1).views =
            cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.VIEWS))
        val attachmentsJson =
            cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.ATTACHMENTS_JSON))
        if (attachmentsJson.nonNullNoEmpty()) {
            val attachmentsEntity = GSON.fromJson(attachmentsJson, AttachmentsEntity::class.java)
            if (attachmentsEntity != null && !attachmentsEntity.entities.isNullOrEmpty()) {
                val all = attachmentsEntity.entities
                val attachmentsOnly: MutableList<Entity> = ArrayList(all.size)
                val copiesOnly: MutableList<PostEntity> = ArrayList(0)
                for (a in all) {
                    if (a is PostEntity) {
                        copiesOnly.add(a)
                    } else {
                        attachmentsOnly.add(a)
                    }
                }
                dbo.attachments = attachmentsOnly
                dbo.copyHistory = copiesOnly
            } else {
                dbo.copyHistory = null
                dbo.attachments = null
            }
        } else {
            dbo.copyHistory = null
            dbo.attachments = null
        }
        return dbo
    }

    companion object {
        fun getCV(dbo: NewsEntity): ContentValues {
            val cv = ContentValues()
            cv.put(NewsColumns.TYPE, dbo.type)
            cv.put(NewsColumns.SOURCE_ID, dbo.sourceId)
            cv.put(NewsColumns.DATE, dbo.date)
            cv.put(NewsColumns.POST_ID, dbo.postId)
            cv.put(NewsColumns.POST_TYPE, dbo.postType)
            cv.put(NewsColumns.FINAL_POST, dbo.isFinalPost)
            cv.put(NewsColumns.COPY_OWNER_ID, dbo.copyOwnerId)
            cv.put(NewsColumns.COPY_POST_ID, dbo.copyPostId)
            cv.put(NewsColumns.COPY_POST_DATE, dbo.copyPostDate)
            cv.put(NewsColumns.TEXT, dbo.text)
            cv.put(NewsColumns.CAN_EDIT, dbo.isCanEdit)
            cv.put(NewsColumns.CAN_DELETE, dbo.isCanDelete)
            cv.put(NewsColumns.COMMENT_COUNT, dbo.commentCount)
            cv.put(NewsColumns.COMMENT_CAN_POST, dbo.isCanPostComment)
            cv.put(NewsColumns.LIKE_COUNT, dbo.likesCount)
            cv.put(NewsColumns.USER_LIKE, dbo.isUserLikes)
            cv.put(NewsColumns.CAN_LIKE, dbo.isCanLike)
            cv.put(NewsColumns.CAN_PUBLISH, dbo.isCanPublish)
            cv.put(NewsColumns.REPOSTS_COUNT, dbo.repostCount)
            cv.put(NewsColumns.USER_REPOSTED, dbo.isUserReposted)
            cv.put(NewsColumns.GEO_ID, dbo.geoId)
            cv.put(
                NewsColumns.TAG_FRIENDS,
                if (dbo.friendsTags != null) join(",", dbo.friendsTags) else null
            )
            cv.put(NewsColumns.VIEWS, dbo.views)
            if (dbo.copyHistory.nonNullNoEmpty() || dbo.attachments.nonNullNoEmpty()) {
                val attachmentsEntities: MutableList<Entity> = ArrayList()
                dbo.attachments.nonNullNoEmpty {
                    attachmentsEntities.addAll(it)
                }
                dbo.copyHistory.nonNullNoEmpty {
                    attachmentsEntities.addAll(it)
                }
                if (attachmentsEntities.nonNullNoEmpty()) {
                    cv.put(
                        NewsColumns.ATTACHMENTS_JSON,
                        GSON.toJson(AttachmentsEntity.from(attachmentsEntities))
                    )
                } else {
                    cv.putNull(NewsColumns.ATTACHMENTS_JSON)
                }
            }
            return cv
        }

        private fun mapList(cursor: Cursor): FeedListEntity {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(FeedListsColumns.TITLE))
            val entity = FeedListEntity(id).setTitle(title)
            val sources =
                cursor.getString(cursor.getColumnIndexOrThrow(FeedListsColumns.SOURCE_IDS))
            var sourceIds: IntArray? = null
            if (sources.nonNullNoEmpty()) {
                val ids = sources.split(",".toRegex()).toTypedArray()
                sourceIds = IntArray(ids.size)
                for (i in ids.indices) {
                    sourceIds[i] = ids[i].toInt()
                }
            }
            return entity.setSourceIds(sourceIds)
                .setNoReposts(cursor.getInt(cursor.getColumnIndexOrThrow(FeedListsColumns.NO_REPOSTS)) == 1)
        }
    }
}