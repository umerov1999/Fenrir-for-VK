package dev.ragnarok.fenrir.db.impl

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFriendListsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getGroupsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getGroupsDetContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getUserContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getUserDetContentUriFor
import dev.ragnarok.fenrir.db.column.*
import dev.ragnarok.fenrir.db.interfaces.IOwnersStorage
import dev.ragnarok.fenrir.db.model.BanAction
import dev.ragnarok.fenrir.db.model.UserPatch
import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.fragment.UserInfoResolveUtil.getUserActivityLine
import dev.ragnarok.fenrir.model.Manager
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.subjects.PublishSubject

internal class OwnersStorage(context: AppStorages) : AbsStorage(context), IOwnersStorage {
    private val banActionsPublisher: PublishSubject<BanAction> = PublishSubject.create()
    private val managementActionsPublisher: PublishSubject<Pair<Int, Manager>> =
        PublishSubject.create()

    override fun fireBanAction(action: BanAction): Completable {
        return Completable.fromAction { banActionsPublisher.onNext(action) }
    }

    override fun observeBanActions(): Observable<BanAction> {
        return banActionsPublisher
    }

    override fun fireManagementChangeAction(manager: Pair<Int, Manager>): Completable {
        return Completable.fromAction { managementActionsPublisher.onNext(manager) }
    }

    override fun observeManagementChanges(): Observable<Pair<Int, Manager>> {
        return managementActionsPublisher
    }

    override fun getUserDetails(accountId: Int, userId: Int): Single<Optional<UserDetailsEntity>> {
        return Single.fromCallable {
            val uri = getUserDetContentUriFor(accountId)
            val where = BaseColumns._ID + " = ?"
            val args = arrayOf(userId.toString())
            val cursor = contentResolver.query(uri, null, where, args, null)
            var details: UserDetailsEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    val json = cursor.getString(cursor.getColumnIndexOrThrow(UsersDetColumns.DATA))
                    if (json.nonNullNoEmpty()) {
                        details = GSON.fromJson(json, UserDetailsEntity::class.java)
                    }
                }
                cursor.close()
            }
            wrap(details)
        }
    }

    override fun getGroupsDetails(
        accountId: Int,
        groupId: Int
    ): Single<Optional<CommunityDetailsEntity>> {
        return Single.fromCallable {
            val uri = getGroupsDetContentUriFor(accountId)
            val where = BaseColumns._ID + " = ?"
            val args = arrayOf(groupId.toString())
            val cursor = contentResolver.query(uri, null, where, args, null)
            var details: CommunityDetailsEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    val json = cursor.getString(cursor.getColumnIndexOrThrow(GroupsDetColumns.DATA))
                    if (json.nonNullNoEmpty()) {
                        details = GSON.fromJson(json, CommunityDetailsEntity::class.java)
                    }
                }
                cursor.close()
            }
            wrap(details)
        }
    }

    override fun storeGroupsDetails(
        accountId: Int,
        groupId: Int,
        dbo: CommunityDetailsEntity?
    ): Completable {
        return Completable.fromAction {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, groupId)
            cv.put(GroupsDetColumns.DATA, GSON.toJson(dbo))
            val uri = getGroupsDetContentUriFor(accountId)
            contentResolver.insert(uri, cv)
        }
    }

    override fun storeUserDetails(
        accountId: Int,
        userId: Int,
        dbo: UserDetailsEntity
    ): Completable {
        return Completable.fromAction {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, userId)
            cv.put(UsersDetColumns.DATA, GSON.toJson(dbo))
            val uri = getUserDetContentUriFor(accountId)
            contentResolver.insert(uri, cv)
        }
    }

    override fun applyPathes(accountId: Int, patches: List<UserPatch>): Completable {
        return if (patches.isEmpty()) {
            Completable.complete()
        } else Completable.create { emitter: CompletableEmitter ->
            val uri = getUserContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>(patches.size)
            for (patch in patches) {
                val cv = ContentValues()
                if (patch.status != null) {
                    cv.put(UserColumns.USER_STATUS, patch.status.status)
                }
                if (patch.online != null) {
                    val online = patch.online
                    cv.put(UserColumns.ONLINE, online.isOnline)
                    cv.put(UserColumns.LAST_SEEN, online.lastSeen)
                    cv.put(UserColumns.PLATFORM, online.platform)
                }
                if (cv.size() > 0) {
                    operations.add(
                        ContentProviderOperation.newUpdate(uri)
                            .withValues(cv)
                            .withSelection(
                                BaseColumns._ID + " = ?",
                                arrayOf(patch.userId.toString())
                            )
                            .build()
                    )
                }
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun findFriendsListsByIds(
        accountId: Int,
        userId: Int,
        ids: Collection<Int>
    ): Single<MutableMap<Int, FriendListEntity>> {
        return Single.create { emitter: SingleEmitter<MutableMap<Int, FriendListEntity>> ->
            val uri = getFriendListsContentUriFor(accountId)
            val where =
                FriendListsColumns.USER_ID + " = ? " + " AND " + FriendListsColumns.LIST_ID + " IN(" + TextUtils.join(
                    ",",
                    ids
                ) + ")"
            val args = arrayOf(userId.toString())
            val cursor = context.contentResolver.query(uri, null, where, args, null)
            @SuppressLint("UseSparseArrays") val map: MutableMap<Int, FriendListEntity> =
                HashMap(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed) {
                        break
                    }
                    val dbo = mapFriendsList(cursor)
                    map[dbo.id] = dbo
                }
                cursor.close()
            }
            emitter.onSuccess(map)
        }
    }

    override fun getLocalizedUserActivity(accountId: Int, userId: Int): Maybe<String> {
        return Maybe.create { e: MaybeEmitter<String> ->
            val uProjection = arrayOf(UserColumns.LAST_SEEN, UserColumns.ONLINE, UserColumns.SEX)
            val uri = getUserContentUriFor(accountId)
            val where = BaseColumns._ID + " = ?"
            val args = arrayOf(userId.toString())
            val cursor = context.contentResolver.query(uri, uProjection, where, args, null)
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    val online =
                        cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.ONLINE)) == 1
                    val lastSeen =
                        cursor.getLong(cursor.getColumnIndexOrThrow(UserColumns.LAST_SEEN))
                    val sex = cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.SEX))
                    val userActivityLine =
                        getUserActivityLine(context, lastSeen, online, sex, false)
                    e.onSuccess(userActivityLine)
                }
                cursor.close()
            }
            e.onComplete()
        }
    }

    override fun findUserDboById(accountId: Int, ownerId: Int): Single<Optional<UserEntity>> {
        return Single.create { emitter: SingleEmitter<Optional<UserEntity>> ->
            val where = BaseColumns._ID + " = ?"
            val args = arrayOf(ownerId.toString())
            val uri = getUserContentUriFor(accountId)
            val cursor = context.contentResolver.query(uri, null, where, args, null)
            var dbo: UserEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    dbo = mapUserDbo(cursor)
                }
                cursor.close()
            }
            emitter.onSuccess(wrap(dbo))
        }
    }

    override fun findCommunityDboById(
        accountId: Int,
        ownerId: Int
    ): Single<Optional<CommunityEntity>> {
        return Single.create { emitter: SingleEmitter<Optional<CommunityEntity>> ->
            val where = BaseColumns._ID + " = ?"
            val args = arrayOf(ownerId.toString())
            val uri = getGroupsContentUriFor(accountId)
            val cursor = context.contentResolver.query(uri, null, where, args, null)
            var dbo: CommunityEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    dbo = mapCommunityDbo(cursor)
                }
                cursor.close()
            }
            emitter.onSuccess(wrap(dbo))
        }
    }

    override fun findUserByDomain(accountId: Int, domain: String?): Single<Optional<UserEntity>> {
        return Single.create { emitter: SingleEmitter<Optional<UserEntity>> ->
            val uri = getUserContentUriFor(accountId)
            val where = UserColumns.DOMAIN + " LIKE ?"
            val args = arrayOf(domain)
            val cursor = contentResolver.query(uri, null, where, args, null)
            var entity: UserEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    entity = mapUserDbo(cursor)
                }
                cursor.close()
            }
            emitter.onSuccess(wrap(entity))
        }
    }

    override fun findCommunityByDomain(
        accountId: Int,
        domain: String?
    ): Single<Optional<CommunityEntity>> {
        return Single.create { emitter: SingleEmitter<Optional<CommunityEntity>> ->
            val uri = getGroupsContentUriFor(accountId)
            val where = GroupColumns.SCREEN_NAME + " LIKE ?"
            val args = arrayOf(domain)
            val cursor = contentResolver.query(uri, null, where, args, null)
            var entity: CommunityEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    entity = mapCommunityDbo(cursor)
                }
                cursor.close()
            }
            emitter.onSuccess(wrap(entity))
        }
    }

    override fun findUserDbosByIds(accountId: Int, ids: List<Int>): Single<List<UserEntity>> {
        return if (ids.isEmpty()) {
            Single.just(emptyList())
        } else Single.create { emitter: SingleEmitter<List<UserEntity>> ->
            val where: String
            val args: Array<String>?
            val uri = getUserContentUriFor(accountId)
            if (ids.size == 1) {
                where = BaseColumns._ID + " = ?"
                args = arrayOf(ids[0].toString())
            } else {
                where = BaseColumns._ID + " IN (" + TextUtils.join(",", ids) + ")"
                args = null
            }
            val cursor = contentResolver.query(uri, null, where, args, null, null)
            val dbos: MutableList<UserEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed) {
                        break
                    }
                    dbos.add(mapUserDbo(cursor))
                }
                cursor.close()
            }
            emitter.onSuccess(dbos)
        }
    }

    override fun findCommunityDbosByIds(
        accountId: Int,
        ids: List<Int>
    ): Single<List<CommunityEntity>> {
        return if (ids.isEmpty()) {
            Single.just(emptyList())
        } else Single.create { emitter: SingleEmitter<List<CommunityEntity>> ->
            val where: String
            val args: Array<String>?
            val uri = getGroupsContentUriFor(accountId)
            if (ids.size == 1) {
                where = BaseColumns._ID + " = ?"
                args = arrayOf(ids[0].toString())
            } else {
                where = BaseColumns._ID + " IN (" + TextUtils.join(",", ids) + ")"
                args = null
            }
            val cursor = contentResolver.query(uri, null, where, args, null, null)
            val dbos: MutableList<CommunityEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed) {
                        break
                    }
                    dbos.add(mapCommunityDbo(cursor))
                }
                cursor.close()
            }
            emitter.onSuccess(dbos)
        }
    }

    override fun storeUserDbos(accountId: Int, users: List<UserEntity>): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val operations = ArrayList<ContentProviderOperation>(users.size)
            appendUsersInsertOperation(operations, accountId, users)
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun storeOwnerEntities(accountId: Int, entities: OwnerEntities?): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            entities ?: return@create emitter.onComplete()
            val operations = ArrayList<ContentProviderOperation>(
                entities.size()
            )
            appendUsersInsertOperation(operations, accountId, entities.userEntities)
            appendCommunitiesInsertOperation(operations, accountId, entities.communityEntities)
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun storeCommunityDbos(
        accountId: Int,
        communityEntities: List<CommunityEntity>
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val operations = ArrayList<ContentProviderOperation>(communityEntities.size)
            appendCommunitiesInsertOperation(operations, accountId, communityEntities)
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun getMissingUserIds(accountId: Int, ids: Collection<Int>): Single<Collection<Int>> {
        return Single.create { e: SingleEmitter<Collection<Int>> ->
            if (ids.isEmpty()) {
                e.onSuccess(emptyList())
                return@create
            }
            val copy: MutableSet<Int> = HashSet(ids)
            val projection = arrayOf(BaseColumns._ID)
            val cursor = contentResolver.query(
                getUserContentUriFor(accountId),
                projection, BaseColumns._ID + " IN ( " + TextUtils.join(",", copy) + ")", null, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    copy.remove(id)
                }
                cursor.close()
            }
            e.onSuccess(copy)
        }
    }

    override fun getMissingCommunityIds(
        accountId: Int,
        ids: Collection<Int>
    ): Single<Collection<Int>> {
        return Single.create { e: SingleEmitter<Collection<Int>> ->
            if (ids.isEmpty()) {
                e.onSuccess(emptyList())
                return@create
            }
            val copy: MutableSet<Int> = HashSet(ids)
            val projection = arrayOf(BaseColumns._ID)
            val cursor = contentResolver.query(
                getGroupsContentUriFor(accountId),
                projection, BaseColumns._ID + " IN ( " + TextUtils.join(",", copy) + ")", null, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    copy.remove(id)
                }
                cursor.close()
            }
            e.onSuccess(copy)
        }
    }

    private fun mapFriendsList(cursor: Cursor): FriendListEntity {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(FriendListsColumns.LIST_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(FriendListsColumns.NAME))
        return FriendListEntity(id, name)
    }

    companion object {
        private fun appendUserInsertOperation(
            operations: MutableList<ContentProviderOperation>,
            uri: Uri,
            dbo: UserEntity
        ) {
            operations.add(
                ContentProviderOperation.newInsert(uri)
                    .withValues(createCv(dbo))
                    .build()
            )
        }

        private fun appendCommunityInsertOperation(
            operations: MutableList<ContentProviderOperation>,
            uri: Uri,
            dbo: CommunityEntity
        ) {
            operations.add(
                ContentProviderOperation.newInsert(uri)
                    .withValues(createCv(dbo))
                    .build()
            )
        }


        fun appendOwnersInsertOperations(
            operations: MutableList<ContentProviderOperation>,
            accountId: Int,
            ownerEntities: OwnerEntities?
        ) {
            ownerEntities ?: return
            appendUsersInsertOperation(operations, accountId, ownerEntities.userEntities)
            appendCommunitiesInsertOperation(operations, accountId, ownerEntities.communityEntities)
        }


        fun appendUsersInsertOperation(
            operations: MutableList<ContentProviderOperation>,
            accouuntId: Int,
            dbos: List<UserEntity>?
        ) {
            dbos ?: return
            val uri = getUserContentUriFor(accouuntId)
            for (dbo in dbos) {
                appendUserInsertOperation(operations, uri, dbo)
            }
        }


        fun appendCommunitiesInsertOperation(
            operations: MutableList<ContentProviderOperation>,
            accouuntId: Int,
            dbos: List<CommunityEntity>?
        ) {
            dbos ?: return
            val uri = getGroupsContentUriFor(accouuntId)
            for (dbo in dbos) {
                appendCommunityInsertOperation(operations, uri, dbo)
            }
        }

        private fun createCv(dbo: CommunityEntity): ContentValues {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, dbo.id)
            cv.put(GroupColumns.NAME, dbo.name)
            cv.put(GroupColumns.SCREEN_NAME, dbo.screenName)
            cv.put(GroupColumns.IS_CLOSED, dbo.closed)
            cv.put(GroupColumns.IS_VERIFIED, dbo.isVerified)
            cv.put(GroupColumns.IS_ADMIN, dbo.isAdmin)
            cv.put(GroupColumns.ADMIN_LEVEL, dbo.adminLevel)
            cv.put(GroupColumns.IS_MEMBER, dbo.isMember)
            cv.put(GroupColumns.MEMBER_STATUS, dbo.memberStatus)
            cv.put(GroupColumns.MEMBERS_COUNT, dbo.membersCount)
            cv.put(GroupColumns.TYPE, dbo.type)
            cv.put(GroupColumns.PHOTO_50, dbo.photo50)
            cv.put(GroupColumns.PHOTO_100, dbo.photo100)
            cv.put(GroupColumns.PHOTO_200, dbo.photo200)
            return cv
        }

        private fun createCv(dbo: UserEntity): ContentValues {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, dbo.id)
            cv.put(UserColumns.FIRST_NAME, dbo.firstName)
            cv.put(UserColumns.LAST_NAME, dbo.lastName)
            cv.put(UserColumns.ONLINE, dbo.isOnline)
            cv.put(UserColumns.ONLINE_MOBILE, dbo.isOnlineMobile)
            cv.put(UserColumns.ONLINE_APP, dbo.onlineApp)
            cv.put(UserColumns.PHOTO_50, dbo.photo50)
            cv.put(UserColumns.PHOTO_100, dbo.photo100)
            cv.put(UserColumns.PHOTO_200, dbo.photo200)
            cv.put(UserColumns.PHOTO_MAX, dbo.photoMax)
            cv.put(UserColumns.LAST_SEEN, dbo.lastSeen)
            cv.put(UserColumns.PLATFORM, dbo.platform)
            cv.put(UserColumns.USER_STATUS, dbo.status)
            cv.put(UserColumns.SEX, dbo.sex)
            cv.put(UserColumns.DOMAIN, dbo.domain)
            cv.put(UserColumns.IS_FRIEND, dbo.isFriend)
            cv.put(UserColumns.FRIEND_STATUS, dbo.friendStatus)
            cv.put(UserColumns.WRITE_MESSAGE_STATUS, dbo.canWritePrivateMessage)
            cv.put(UserColumns.IS_USER_BLACK_LIST, dbo.blacklisted_by_me)
            cv.put(UserColumns.IS_BLACK_LISTED, dbo.blacklisted)
            cv.put(UserColumns.IS_VERIFIED, dbo.isVerified)
            cv.put(UserColumns.IS_CAN_ACCESS_CLOSED, dbo.isCan_access_closed)
            cv.put(UserColumns.MAIDEN_NAME, dbo.maiden_name)
            return cv
        }

        private fun mapCommunityDbo(cursor: Cursor): CommunityEntity {
            return CommunityEntity(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                .setName(cursor.getString(cursor.getColumnIndexOrThrow(GroupColumns.NAME)))
                .setScreenName(cursor.getString(cursor.getColumnIndexOrThrow(GroupColumns.SCREEN_NAME)))
                .setClosed(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.IS_CLOSED)))
                .setVerified(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.IS_VERIFIED)) == 1)
                .setAdmin(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.IS_ADMIN)) == 1)
                .setAdminLevel(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.ADMIN_LEVEL)))
                .setMember(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.IS_MEMBER)) == 1)
                .setMemberStatus(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.MEMBER_STATUS)))
                .setMembersCount(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.MEMBERS_COUNT)))
                .setType(cursor.getInt(cursor.getColumnIndexOrThrow(GroupColumns.TYPE)))
                .setPhoto50(cursor.getString(cursor.getColumnIndexOrThrow(GroupColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(GroupColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(GroupColumns.PHOTO_200)))
        }

        private fun mapUserDbo(cursor: Cursor): UserEntity {
            return UserEntity(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                .setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.FIRST_NAME)))
                .setLastName(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.LAST_NAME)))
                .setOnline(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.ONLINE)) == 1)
                .setOnlineMobile(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.ONLINE_MOBILE)) == 1)
                .setOnlineApp(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.ONLINE_APP)))
                .setPhoto50(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.PHOTO_200)))
                .setPhotoMax(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.PHOTO_MAX)))
                .setLastSeen(cursor.getLong(cursor.getColumnIndexOrThrow(UserColumns.LAST_SEEN)))
                .setPlatform(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.PLATFORM)))
                .setStatus(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.USER_STATUS)))
                .setSex(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.SEX)))
                .setDomain(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.DOMAIN)))
                .setFriend(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.IS_FRIEND)) == 1)
                .setFriendStatus(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.FRIEND_STATUS)))
                .setCanWritePrivateMessage(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.WRITE_MESSAGE_STATUS)) == 1)
                .setBlacklisted_by_me(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.IS_USER_BLACK_LIST)) == 1)
                .setBlacklisted(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.IS_BLACK_LISTED)) == 1)
                .setVerified(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.IS_VERIFIED)) == 1)
                .setCan_access_closed(cursor.getInt(cursor.getColumnIndexOrThrow(UserColumns.IS_CAN_ACCESS_CLOSED)) == 1)
                .setMaiden_name(cursor.getString(cursor.getColumnIndexOrThrow(UserColumns.MAIDEN_NAME)))
        }
    }

}