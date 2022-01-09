package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.GroupSettingsDto;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiGroupChats;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiBanned;
import dev.ragnarok.fenrir.api.model.VkApiMarket;
import dev.ragnarok.fenrir.api.model.VkApiMarketAlbum;
import dev.ragnarok.fenrir.api.model.response.GroupLongpollServer;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IGroupsApi {

    @CheckResult
    Completable editManager(int groupId, int userId, String role, Boolean isContact, String contactPosition, String contactPhone, String contactEmail);

    @CheckResult
    Completable unban(int groupId, int ownerId);

    @CheckResult
    Completable ban(int groupId, int ownerId, Long endDate, Integer reason, String comment, Boolean commentVisible);

    @CheckResult
    Single<GroupSettingsDto> getSettings(int groupId);

    @CheckResult
    Single<Items<VkApiMarketAlbum>> getMarketAlbums(int owner_id, int offset, int count);

    @CheckResult
    Single<Items<VkApiMarket>> getMarket(int owner_id, int album_id, int offset, int count, Integer extended);

    @CheckResult
    Single<Items<VkApiMarket>> getMarketById(Collection<AccessIdPair> ids);

    @CheckResult
    Single<Items<VkApiBanned>> getBanned(int groupId, Integer offset, Integer count, String fields, Integer userId);

    @CheckResult
    Single<VKApiCommunity> getWallInfo(String groupId, String fields);

    @CheckResult
    Single<Items<VKApiUser>> getMembers(String groupId, Integer sort, Integer offset,
                                        Integer count, String fields, String filter);

    @CheckResult
    Single<Items<VKApiCommunity>> search(String query, String type, String filter, Integer countryId, Integer cityId,
                                         Boolean future, Boolean market, Integer sort, Integer offset, Integer count);

    @CheckResult
    Single<Boolean> leave(int groupId);

    @CheckResult
    Single<Boolean> join(int groupId, Integer notSure);

    @CheckResult
    Single<Items<VKApiCommunity>> get(Integer userId, Boolean extended, String filter,
                                      String fields, Integer offset, Integer count);

    @CheckResult
    Single<List<VKApiCommunity>> getById(Collection<Integer> ids, Collection<String> domains,
                                         String groupId, String fields);

    @CheckResult
    Single<GroupLongpollServer> getLongPollServer(int groupId);

    @CheckResult
    Single<Items<VKApiGroupChats>> getChats(int groupId, Integer offset, Integer count);
}
