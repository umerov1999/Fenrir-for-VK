package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.db.model.BanAction;
import dev.ragnarok.fenrir.db.model.UserPatch;
import dev.ragnarok.fenrir.db.model.entity.CommunityDetailsEntity;
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity;
import dev.ragnarok.fenrir.db.model.entity.FriendListEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.UserDetailsEntity;
import dev.ragnarok.fenrir.db.model.entity.UserEntity;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface IOwnersStorage extends IStorage {

    Single<Map<Integer, FriendListEntity>> findFriendsListsByIds(int accountId, int userId, Collection<Integer> ids);

    @CheckResult
    Maybe<String> getLocalizedUserActivity(int accountId, int userId);

    Single<Optional<UserEntity>> findUserDboById(int accountId, int ownerId);

    Single<Optional<CommunityEntity>> findCommunityDboById(int accountId, int ownerId);

    Single<Optional<UserEntity>> findUserByDomain(int accountId, String domain);

    Single<Optional<CommunityEntity>> findCommunityByDomain(int accountId, String domain);

    Single<List<UserEntity>> findUserDbosByIds(int accountId, List<Integer> ids);

    Single<List<CommunityEntity>> findCommunityDbosByIds(int accountId, List<Integer> ids);

    Completable storeUserDbos(int accountId, List<UserEntity> users);

    Completable storeCommunityDbos(int accountId, List<CommunityEntity> communityEntities);

    Completable storeOwnerEntities(int accountId, OwnerEntities entities);

    @CheckResult
    Single<Collection<Integer>> getMissingUserIds(int accountId, @NonNull Collection<Integer> ids);

    @CheckResult
    Single<Collection<Integer>> getMissingCommunityIds(int accountId, @NonNull Collection<Integer> ids);

    Completable fireBanAction(BanAction action);

    Observable<BanAction> observeBanActions();

    Completable fireManagementChangeAction(Pair<Integer, Manager> manager);

    Observable<Pair<Integer, Manager>> observeManagementChanges();

    Single<Optional<CommunityDetailsEntity>> getGroupsDetails(int accountId, int groupId);

    Completable storeGroupsDetails(int accountId, int groupId, CommunityDetailsEntity dbo);

    Single<Optional<UserDetailsEntity>> getUserDetails(int accountId, int userId);

    Completable storeUserDetails(int accountId, int userId, UserDetailsEntity dbo);

    Completable applyPathes(int accountId, @NonNull List<UserPatch> patches);
}