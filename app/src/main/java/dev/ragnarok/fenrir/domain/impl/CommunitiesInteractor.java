package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.db.column.GroupColumns;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity;
import dev.ragnarok.fenrir.domain.ICommunitiesInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public class CommunitiesInteractor implements ICommunitiesInteractor {

    private final INetworker networker;
    private final IStorages stores;

    public CommunitiesInteractor(INetworker networker, IStorages repositories) {
        this.networker = networker;
        stores = repositories;
    }

    @Override
    public Single<List<Community>> getCachedData(int accountId, int userId) {
        return stores.relativeship()
                .getCommunities(accountId, userId)
                .map(Entity2Model::buildCommunitiesFromDbos);
    }

    @Override
    public Single<List<Community>> getActual(int accountId, int userId, int count, int offset) {
        return networker.vkDefault(accountId)
                .groups()
                .get(userId, true, null, GroupColumns.API_FIELDS, offset, count)
                .flatMap(items -> {
                    List<VKApiCommunity> dtos = listEmptyIfNull(items.getItems());
                    List<CommunityEntity> dbos = Dto2Entity.mapCommunities(dtos);

                    return stores.relativeship()
                            .storeComminities(accountId, dbos, userId, offset == 0)
                            .andThen(Single.just(Entity2Model.buildCommunitiesFromDbos(dbos)));
                });
    }

    @Override
    public Single<List<Owner>> getGroupFriends(int accountId, int groupId) {
        return networker.vkDefault(accountId)
                .groups()
                .getMembers(String.valueOf(groupId), null, 0, 1000, Constants.MAIN_OWNER_FIELDS, "friends")
                .map(items -> {
                    List<VKApiUser> dtos = listEmptyIfNull(items.getItems());
                    return Dto2Model.transformOwners(dtos, null);
                });
    }

    @Override
    public Single<List<Community>> search(int accountId, String q, String type, Integer countryId, Integer cityId, Boolean futureOnly, Integer sort, int count, int offset) {
        return networker.vkDefault(accountId)
                .groups()
                .search(q, type, GroupColumns.API_FIELDS, countryId, cityId, futureOnly, null, sort, offset, count)
                .map(items -> {
                    List<VKApiCommunity> dtos = listEmptyIfNull(items.getItems());
                    return Dto2Model.transformCommunities(dtos);
                });
    }

    @Override
    public Completable join(int accountId, int groupId) {
        return networker.vkDefault(accountId)
                .groups()
                .join(groupId, null)
                .ignoreElement();
    }

    @Override
    public Completable leave(int accountId, int groupId) {
        return networker.vkDefault(accountId)
                .groups()
                .leave(groupId)
                .ignoreElement();
    }
}