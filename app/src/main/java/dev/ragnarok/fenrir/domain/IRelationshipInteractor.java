package dev.ragnarok.fenrir.domain;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.FriendsCounters;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public interface IRelationshipInteractor {
    int FRIEND_ADD_REQUEST_SENT = 1;
    int FRIEND_ADD_REQUEST_FROM_USER_APPROVED = 2;
    int FRIEND_ADD_RESENDING = 4;

    Single<List<User>> getCachedFriends(int accountId, int objectId);

    Single<List<User>> getCachedFollowers(int accountId, int objectId);

    Single<List<User>> getCachedRequests(int accountId);

    Single<List<User>> getActualFriendsList(int accountId, int objectId, Integer count, int offset);

    Single<List<User>> getOnlineFriends(int accountId, int objectId, int count, int offset);

    Single<List<User>> getRecommendations(int accountId, Integer count);

    Single<List<User>> getByPhones(int accountId, @NonNull Context context);

    Single<List<User>> getFollowers(int accountId, int objectId, int count, int offset);

    Single<List<User>> getMutualFriends(int accountId, int objectId, int count, int offset);

    Single<List<User>> getRequests(int accountId, Integer offset, Integer count);

    Single<Pair<List<User>, Integer>> searchFriends(int accountId, int userId, int count, int offset, String q);

    Single<FriendsCounters> getFriendsCounters(int accountId, int userId);

    Single<Integer> addFriend(int accountId, int userId, String optionalText, boolean keepFollow);

    Single<Integer> deleteFriends(int accountId, int userId);

    interface DeletedCodes {
        int FRIEND_DELETED = 1;
        int OUT_REQUEST_DELETED = 2;
        int IN_REQUEST_DELETED = 3;
        int SUGGESTION_DELETED = 4;
    }
}