package dev.ragnarok.fenrir.domain.impl

import android.content.Context
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiUser
import dev.ragnarok.fenrir.api.model.response.DeleteFriendResponse
import dev.ragnarok.fenrir.api.model.response.OnlineFriendsResponse
import dev.ragnarok.fenrir.db.column.UserColumns
import dev.ragnarok.fenrir.db.impl.ContactsUtils.getAllContacts
import dev.ragnarok.fenrir.db.interfaces.IStorages
import dev.ragnarok.fenrir.db.model.entity.UserEntity
import dev.ragnarok.fenrir.domain.IRelationshipInteractor
import dev.ragnarok.fenrir.domain.IRelationshipInteractor.DeletedCodes
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapUsers
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transformUsers
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.buildUsersFromDbo
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.exception.UnepectedResultException
import dev.ragnarok.fenrir.model.FriendsCounters
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import io.reactivex.rxjava3.core.Single

class RelationshipInteractor(
    private val repositories: IStorages,
    private val networker: INetworker
) : IRelationshipInteractor {
    override fun getCachedFriends(accountId: Int, objectId: Int): Single<List<User>> {
        return repositories.relativeship()
            .getFriends(accountId, objectId)
            .map { obj: List<UserEntity> -> buildUsersFromDbo(obj) }
    }

    override fun getCachedFollowers(accountId: Int, objectId: Int): Single<List<User>> {
        return repositories.relativeship()
            .getFollowers(accountId, objectId)
            .map { obj: List<UserEntity> -> buildUsersFromDbo(obj) }
    }

    override fun getCachedRequests(accountId: Int): Single<List<User>> {
        return repositories.relativeship()
            .getRequests(accountId)
            .map { obj: List<UserEntity> -> buildUsersFromDbo(obj) }
    }

    override fun getActualFriendsList(
        accountId: Int,
        objectId: Int,
        count: Int?,
        offset: Int
    ): Single<List<User>> {
        val order = if (accountId == objectId) "hints" else null
        return networker.vkDefault(accountId)
            .friends()[objectId, order, null, count, offset, UserColumns.API_FIELDS, null]
            .map { items: Items<VKApiUser> -> listEmptyIfNull(items.getItems()) }
            .flatMap { dtos: List<VKApiUser> ->
                val dbos = mapUsers(dtos)
                val users = transformUsers(dtos)
                repositories.relativeship()
                    .storeFriends(accountId, dbos, objectId, offset == 0)
                    .andThen(Single.just(users))
            }
    }

    override fun getOnlineFriends(
        accountId: Int,
        objectId: Int,
        count: Int,
        offset: Int
    ): Single<List<User>> {
        val order =
            if (accountId == objectId) "hints" else null // hints (сортировка по популярности) доступна только для своих друзей
        return networker.vkDefault(accountId)
            .friends()
            .getOnline(objectId, order, count, offset, UserColumns.API_FIELDS)
            .map { response: OnlineFriendsResponse -> listEmptyIfNull(response.profiles) }
            .map { obj: List<VKApiUser> -> transformUsers(obj) }
    }

    override fun getRecommendations(accountId: Int, count: Int?): Single<List<User>> {
        return networker.vkDefault(accountId)
            .friends()
            .getRecommendations(count, UserColumns.API_FIELDS, null)
            .map { response: Items<VKApiUser> -> listEmptyIfNull(response.items) }
            .map { obj: List<VKApiUser> -> transformUsers(obj) }
    }

    override fun getByPhones(accountId: Int, context: Context): Single<List<User>> {
        return getAllContacts(context).flatMap { t: String? ->
            networker.vkDefault(accountId)
                .friends()
                .getByPhones(t, UserColumns.API_FIELDS)
                .map { obj: List<VKApiUser>? -> listEmptyIfNull(obj) }
                .map { obj: List<VKApiUser> -> transformUsers(obj) }
        }
    }

    override fun getFollowers(
        accountId: Int,
        objectId: Int,
        count: Int,
        offset: Int
    ): Single<List<User>> {
        return networker.vkDefault(accountId)
            .users()
            .getFollowers(objectId, offset, count, UserColumns.API_FIELDS, null)
            .map { items: Items<VKApiUser>? -> listEmptyIfNull(items?.getItems()) }
            .flatMap { dtos: List<VKApiUser> ->
                val dbos = mapUsers(dtos)
                val users = transformUsers(dtos)
                repositories.relativeship()
                    .storeFollowers(accountId, dbos, objectId, offset == 0)
                    .andThen(Single.just(users))
            }
    }

    override fun getRequests(accountId: Int, offset: Int?, count: Int?): Single<List<User>> {
        return networker.vkDefault(accountId)
            .users()
            .getRequests(offset, count, 1, 1, UserColumns.API_FIELDS)
            .map { items: Items<VKApiUser>? -> listEmptyIfNull(items?.getItems()) }
            .flatMap { dtos: List<VKApiUser> ->
                val dbos = mapUsers(dtos)
                val users = transformUsers(dtos)
                repositories.relativeship()
                    .storeRequests(accountId, dbos, accountId, offset == 0)
                    .andThen(Single.just(users))
            }
    }

    override fun getMutualFriends(
        accountId: Int,
        objectId: Int,
        count: Int,
        offset: Int
    ): Single<List<User>> {
        return networker.vkDefault(accountId)
            .friends()
            .getMutual(accountId, objectId, count, offset, UserColumns.API_FIELDS)
            .map { obj: List<VKApiUser> -> transformUsers(obj) }
    }

    override fun searchFriends(
        accountId: Int,
        userId: Int,
        count: Int,
        offset: Int,
        q: String?
    ): Single<Pair<List<User>, Int>> {
        return networker.vkDefault(accountId)
            .friends()
            .search(userId, q, UserColumns.API_FIELDS, null, offset, count)
            .map { items: Items<VKApiUser>? ->
                val users = transformUsers(listEmptyIfNull(items?.getItems()))
                create(users, items?.getCount() ?: 0)
            }
    }

    override fun getFriendsCounters(accountId: Int, userId: Int): Single<FriendsCounters> {
        return networker.vkDefault(accountId)
            .users()[listOf(userId), null, "counters", null]
            .map { users: List<VKApiUser> ->
                if (users.isEmpty()) {
                    throw NotFoundException()
                }
                val user = users[0]
                val counters: FriendsCounters = if (user.counters != null) {
                    FriendsCounters(
                        user.counters.friends,
                        user.counters.online_friends,
                        user.counters.followers,
                        user.counters.mutual_friends
                    )
                } else {
                    FriendsCounters(0, 0, 0, 0)
                }
                counters
            }
    }

    override fun addFriend(
        accountId: Int,
        userId: Int,
        optionalText: String?,
        keepFollow: Boolean
    ): Single<Int> {
        return networker.vkDefault(accountId)
            .friends()
            .add(userId, optionalText, keepFollow)
    }

    override fun deleteFriends(accountId: Int, userId: Int): Single<Int> {
        return networker.vkDefault(accountId)
            .friends()
            .delete(userId)
            .map { response: DeleteFriendResponse ->
                if (response.friend_deleted) {
                    return@map DeletedCodes.FRIEND_DELETED
                }
                if (response.in_request_deleted) {
                    return@map DeletedCodes.IN_REQUEST_DELETED
                }
                if (response.out_request_deleted) {
                    return@map DeletedCodes.OUT_REQUEST_DELETED
                }
                if (response.suggestion_deleted) {
                    return@map DeletedCodes.SUGGESTION_DELETED
                }
                throw UnepectedResultException()
            }
    }
}