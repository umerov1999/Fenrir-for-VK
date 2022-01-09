package dev.ragnarok.fenrir.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.longpoll.UserIsOfflineUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.UserIsOnlineUpdate;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.fragment.search.criteria.PeopleSearchCriteria;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.CommunityDetails;
import dev.ragnarok.fenrir.model.Gift;
import dev.ragnarok.fenrir.model.GroupChats;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserDetails;
import dev.ragnarok.fenrir.model.UserUpdate;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


public interface IOwnersRepository {

    int MODE_ANY = 1;

    int MODE_NET = 2;

    int MODE_CACHE = 3;

    Single<List<Owner>> findBaseOwnersDataAsList(int accountId, Collection<Integer> ids, int mode);

    Single<IOwnersBundle> findBaseOwnersDataAsBundle(int accountId, Collection<Integer> ids, int mode);

    Single<IOwnersBundle> findBaseOwnersDataAsBundle(int accountId, Collection<Integer> ids, int mode, Collection<? extends Owner> alreadyExists);

    Single<Owner> getBaseOwnerInfo(int accountId, int ownerId, int mode);

    Single<Pair<User, UserDetails>> getFullUserInfo(int accountId, int userId, int mode);

    Single<List<MarketAlbum>> getMarketAlbums(int accountId, int owner_id, int offset, int count);

    Single<List<Market>> getMarket(int accountId, int owner_id, int album_id, int offset, int count);

    Single<List<Gift>> getGifts(int accountId, int user_id, int count, int offset);

    Single<List<Market>> getMarketById(int accountId, Collection<AccessIdPair> ids);

    Single<Pair<Community, CommunityDetails>> getFullCommunityInfo(int accountId, int communityId, int mode);

    Completable cacheActualOwnersData(int accountId, Collection<Integer> ids);

    Single<List<Owner>> getCommunitiesWhereAdmin(int accountId, boolean admin, boolean editor, boolean moderator);

    Single<List<User>> searchPeoples(int accountId, PeopleSearchCriteria criteria, int count, int offset);

    Completable insertOwners(int accountId, @NonNull OwnerEntities entities);

    Completable handleStatusChange(int accountId, int userId, String status);

    Completable handleOnlineChanges(int accountId, @Nullable List<UserIsOfflineUpdate> offlineUpdates, @Nullable List<UserIsOnlineUpdate> onlineUpdates);

    Flowable<List<UserUpdate>> observeUpdates();

    Single<Integer> report(int accountId, int userId, String type, String comment);

    Single<Integer> checkAndAddFriend(int accountId, int userId);

    Single<List<Story>> getStory(int accountId, Integer owner_id);

    Single<List<Story>> searchStory(int accountId, String q, Integer mentioned_id);

    Single<List<GroupChats>> getGroupChats(int accountId, int groupId, Integer offset, Integer count);
}