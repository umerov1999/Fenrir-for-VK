package dev.ragnarok.fenrir.domain

import android.content.Context
import dev.ragnarok.fenrir.model.FriendsCounters
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.util.Pair
import io.reactivex.rxjava3.core.Single

interface IRelationshipInteractor {
    fun getCachedFriends(accountId: Int, objectId: Int): Single<List<User>>
    fun getCachedFollowers(accountId: Int, objectId: Int): Single<List<User>>
    fun getCachedRequests(accountId: Int): Single<List<User>>
    fun getActualFriendsList(
        accountId: Int,
        objectId: Int,
        count: Int?,
        offset: Int
    ): Single<List<User>>

    fun getOnlineFriends(
        accountId: Int,
        objectId: Int,
        count: Int,
        offset: Int
    ): Single<List<User>>

    fun getRecommendations(accountId: Int, count: Int?): Single<List<User>>
    fun getByPhones(accountId: Int, context: Context): Single<List<User>>
    fun getFollowers(accountId: Int, objectId: Int, count: Int, offset: Int): Single<List<User>>
    fun getMutualFriends(
        accountId: Int,
        objectId: Int,
        count: Int,
        offset: Int
    ): Single<List<User>>

    fun getRequests(accountId: Int, offset: Int?, count: Int?): Single<List<User>>
    fun searchFriends(
        accountId: Int,
        userId: Int,
        count: Int,
        offset: Int,
        q: String?
    ): Single<Pair<List<User>, Int>>

    fun getFriendsCounters(accountId: Int, userId: Int): Single<FriendsCounters>
    fun addFriend(
        accountId: Int,
        userId: Int,
        optionalText: String?,
        keepFollow: Boolean
    ): Single<Int>

    fun deleteFriends(accountId: Int, userId: Int): Single<Int>
    interface DeletedCodes {
        companion object {
            const val FRIEND_DELETED = 1
            const val OUT_REQUEST_DELETED = 2
            const val IN_REQUEST_DELETED = 3
            const val SUGGESTION_DELETED = 4
        }
    }

    companion object {
        const val FRIEND_ADD_REQUEST_SENT = 1
        const val FRIEND_ADD_REQUEST_FROM_USER_APPROVED = 2
        const val FRIEND_ADD_RESENDING = 4
    }
}