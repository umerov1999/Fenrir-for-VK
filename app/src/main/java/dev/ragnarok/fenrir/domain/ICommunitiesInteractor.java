package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface ICommunitiesInteractor {
    Single<List<Community>> getCachedData(int accountId, int userId);

    Single<List<Community>> getActual(int accountId, int userId, int count, int offset);

    Single<List<Owner>> getGroupFriends(int accountId, int groupId);

    Single<List<Community>> search(int accountId, String q, String type, Integer countryId, Integer cityId, Boolean futureOnly,
                                   Integer sort, int count, int offset);

    Completable join(int accountId, int groupId);

    Completable leave(int accountId, int groupId);
}