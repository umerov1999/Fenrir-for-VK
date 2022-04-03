package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFriendListsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getRelativeshipContentUriFor
import dev.ragnarok.fenrir.db.column.FriendListsColumns
import dev.ragnarok.fenrir.db.column.RelationshipColumns
import dev.ragnarok.fenrir.db.column.RelationshipColumns.getCV
import dev.ragnarok.fenrir.db.impl.OwnersStorage.Companion.appendCommunitiesInsertOperation
import dev.ragnarok.fenrir.db.impl.OwnersStorage.Companion.appendUsersInsertOperation
import dev.ragnarok.fenrir.db.interfaces.IRelativeshipStorage
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity
import dev.ragnarok.fenrir.db.model.entity.FriendListEntity
import dev.ragnarok.fenrir.db.model.entity.UserEntity
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import kotlin.math.abs

internal class RelativeshipStorage(base: AppStorages) : AbsStorage(base), IRelativeshipStorage {
    override fun storeFriendsList(
        accountId: Int,
        userId: Int,
        data: Collection<FriendListEntity>
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val uri = getFriendListsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>(data.size)
            operations.add(
                ContentProviderOperation.newDelete(uri)
                    .withSelection(
                        FriendListsColumns.FULL_USER_ID + " = ?",
                        arrayOf(userId.toString())
                    )
                    .build()
            )
            for (item in data) {
                val cv = ContentValues()
                cv.put(FriendListsColumns.LIST_ID, item.id)
                cv.put(FriendListsColumns.NAME, item.name)
                operations.add(
                    ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
        }
    }

    override fun storeFriends(
        accountId: Int,
        users: List<UserEntity>,
        objectId: Int,
        clearBeforeStore: Boolean
    ): Completable {
        return completableStoreForType(
            accountId,
            users,
            objectId,
            RelationshipColumns.TYPE_FRIEND,
            clearBeforeStore
        )
    }

    private fun completableStoreForType(
        accountId: Int,
        userEntities: List<UserEntity>,
        objectId: Int,
        relationType: Int,
        clear: Boolean
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val uri = getRelativeshipContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clear) {
                operations.add(clearOperationFor(accountId, objectId, relationType))
            }
            appendInsertHeaders(uri, operations, objectId, userEntities, relationType)
            appendUsersInsertOperation(operations, accountId, userEntities)
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun storeFollowers(
        accountId: Int,
        users: List<UserEntity>,
        objectId: Int,
        clearBeforeStore: Boolean
    ): Completable {
        return completableStoreForType(
            accountId,
            users,
            objectId,
            RelationshipColumns.TYPE_FOLLOWER,
            clearBeforeStore
        )
    }

    override fun storeRequests(
        accountId: Int,
        users: List<UserEntity>,
        objectId: Int,
        clearBeforeStore: Boolean
    ): Completable {
        return completableStoreForType(
            accountId,
            users,
            objectId,
            RelationshipColumns.TYPE_REQUESTS,
            clearBeforeStore
        )
    }

    override fun getFriends(accountId: Int, objectId: Int): Single<List<UserEntity>> {
        return getUsersForType(accountId, objectId, RelationshipColumns.TYPE_FRIEND)
    }

    override fun getFollowers(accountId: Int, objectId: Int): Single<List<UserEntity>> {
        return getUsersForType(accountId, objectId, RelationshipColumns.TYPE_FOLLOWER)
    }

    override fun getRequests(accountId: Int): Single<List<UserEntity>> {
        return getUsersForType(accountId, accountId, RelationshipColumns.TYPE_REQUESTS)
    }

    override fun getCommunities(accountId: Int, ownerId: Int): Single<List<CommunityEntity>> {
        return Single.create { emitter: SingleEmitter<List<CommunityEntity>> ->
            val cursor = getCursorForType(accountId, ownerId, RelationshipColumns.TYPE_MEMBER)
            val dbos: MutableList<CommunityEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed) {
                        break
                    }
                    dbos.add(mapCommunity(cursor))
                }
                cursor.close()
            }
            emitter.onSuccess(dbos)
        }
    }

    override fun storeComminities(
        accountId: Int,
        communities: List<CommunityEntity>,
        userId: Int,
        invalidateBefore: Boolean
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val uri = getRelativeshipContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>(communities.size * 2 + 1)
            if (invalidateBefore) {
                operations.add(
                    clearOperationFor(
                        accountId,
                        userId,
                        RelationshipColumns.TYPE_MEMBER
                    )
                )
            }
            for (dbo in communities) {
                operations.add(
                    ContentProviderOperation.newInsert(uri)
                        .withValues(getCV(userId, -dbo.id, RelationshipColumns.TYPE_MEMBER))
                        .build()
                )
            }
            appendCommunitiesInsertOperation(operations, accountId, communities)
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    private fun getCursorForType(accountId: Int, objectId: Int, relationType: Int): Cursor? {
        val uri = getRelativeshipContentUriFor(accountId)
        val where =
            RelationshipColumns.FULL_TYPE + " = ? AND " + RelationshipColumns.OBJECT_ID + " = ?"
        val args = arrayOf(relationType.toString(), objectId.toString())
        return contentResolver.query(uri, null, where, args, null)
    }

    private fun getUsersForType(
        accountId: Int,
        objectId: Int,
        relationType: Int
    ): Single<List<UserEntity>> {
        return Single.create { emitter: SingleEmitter<List<UserEntity>> ->
            val cursor = getCursorForType(accountId, objectId, relationType)
            val dbos: MutableList<UserEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed) {
                        break
                    }
                    dbos.add(mapDbo(cursor))
                }
                cursor.close()
            }
            emitter.onSuccess(dbos)
        }
    }

    private fun appendInsertHeaders(
        uri: Uri,
        operations: MutableList<ContentProviderOperation>,
        objectId: Int,
        dbos: List<UserEntity>,
        type: Int
    ) {
        for (dbo in dbos) {
            operations.add(
                ContentProviderOperation
                    .newInsert(uri)
                    .withValues(getCV(objectId, dbo.id, type))
                    .build()
            )
        }
    }

    private fun clearOperationFor(
        accountId: Int,
        objectId: Int,
        type: Int
    ): ContentProviderOperation {
        val uri = getRelativeshipContentUriFor(accountId)
        val clearWhere =
            RelationshipColumns.OBJECT_ID + " = ? AND " + RelationshipColumns.TYPE + " = ?"
        val clearWhereArgs = arrayOf(objectId.toString(), type.toString())
        return ContentProviderOperation
            .newDelete(uri)
            .withSelection(clearWhere, clearWhereArgs)
            .build()
    }

    companion object {
        private fun mapCommunity(cursor: Cursor): CommunityEntity {
            return CommunityEntity(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.SUBJECT_ID)))
                .setName(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_NAME)))
                .setScreenName(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_SCREEN_NAME)))
                .setClosed(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_CLOSED)))
                .setVerified(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_VERIFIED)) == 1)
                .setAdmin(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_ADMIN)) == 1)
                .setAdminLevel(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_ADMIN_LEVEL)))
                .setMember(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_MEMBER)) == 1)
                .setMemberStatus(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBER_STATUS)))
                .setMembersCount(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBERS_COUNT)))
                .setType(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_TYPE)))
                .setPhoto50(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_200)))
        }

        private fun mapDbo(cursor: Cursor): UserEntity {
            val gid =
                abs(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.SUBJECT_ID)))
            return UserEntity(gid)
                .setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_FIRST_NAME)))
                .setLastName(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_NAME)))
                .setOnline(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE)) == 1)
                .setOnlineMobile(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE)) == 1)
                .setOnlineApp(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE_APP)))
                .setPhoto50(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_200)))
                .setPhotoMax(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_MAX)))
                .setLastSeen(cursor.getLong(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_SEEN)))
                .setPlatform(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_PLATFORM)))
                .setStatus(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_STATUS)))
                .setSex(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_SEX)))
                .setFriend(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_IS_FRIEND)) == 1)
                .setFriendStatus(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_USER_FRIEND_STATUS)))
                .setCanWritePrivateMessage(
                    cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                            RelationshipColumns.FOREIGN_SUBJECT_WRITE_MESSAGE_STATUS
                        )
                    ) == 1
                )
                .setBlacklisted_by_me(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_IS_USER_BLACK_LIST)) == 1)
                .setBlacklisted(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_IS_BLACK_LISTED)) == 1)
                .setCan_access_closed(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_IS_CAN_ACCESS_CLOSED)) == 1)
                .setVerified(cursor.getInt(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_IS_VERIFIED)) == 1)
                .setMaiden_name(cursor.getString(cursor.getColumnIndexOrThrow(RelationshipColumns.FOREIGN_SUBJECT_MAIDEN_NAME)))
        }
    }
}