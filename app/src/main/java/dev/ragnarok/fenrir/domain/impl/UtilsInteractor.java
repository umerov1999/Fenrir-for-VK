package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiCheckedLink;
import dev.ragnarok.fenrir.api.model.VkApiFriendList;
import dev.ragnarok.fenrir.api.model.response.VkApiChatResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiLinkResponse;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.entity.FriendListEntity;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IUtilsInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.FriendList;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Privacy;
import dev.ragnarok.fenrir.model.ShortLink;
import dev.ragnarok.fenrir.model.SimplePrivacy;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class UtilsInteractor implements IUtilsInteractor {

    private final INetworker networker;
    private final IStorages stores;
    private final IOwnersRepository ownersRepository;

    public UtilsInteractor(INetworker networker, IStorages stores, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.stores = stores;
        this.ownersRepository = ownersRepository;
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public Single<Map<Integer, Privacy>> createFullPrivacies(int accountId, @NonNull Map<Integer, SimplePrivacy> orig) {
        return Single.just(orig)
                .flatMap(map -> {
                    Set<Integer> uids = new HashSet<>();
                    Set<Integer> listsIds = new HashSet<>();

                    for (Map.Entry<?, SimplePrivacy> mapEntry : orig.entrySet()) {
                        SimplePrivacy privacy = mapEntry.getValue();

                        if (isNull(privacy) || isEmpty(privacy.getEntries())) {
                            continue;
                        }

                        for (SimplePrivacy.Entry entry : privacy.getEntries()) {
                            switch (entry.getType()) {
                                case SimplePrivacy.Entry.TYPE_FRIENDS_LIST:
                                    listsIds.add(entry.getId());
                                    break;
                                case SimplePrivacy.Entry.TYPE_USER:
                                    uids.add(entry.getId());
                                    break;
                            }
                        }
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, uids, IOwnersRepository.MODE_ANY)
                            .flatMap(owners -> findFriendListsByIds(accountId, accountId, listsIds)
                                    .map(lists -> {
                                        Map<Integer, Privacy> privacies = new HashMap<>(Utils.safeCountOf(orig));

                                        for (Map.Entry<Integer, SimplePrivacy> entry : orig.entrySet()) {
                                            Integer key = entry.getKey();
                                            SimplePrivacy value = entry.getValue();

                                            Privacy full = isNull(value) ? null : Dto2Model.transform(value, owners, lists);
                                            privacies.put(key, full);
                                        }

                                        return privacies;
                                    }));
                });
    }

    @Override
    public Single<Optional<Owner>> resolveDomain(int accountId, String domain) {
        return stores.owners()
                .findUserByDomain(accountId, domain)
                .<Optional<Owner>>flatMap(optionalUserEntity -> {
                    if (optionalUserEntity.nonEmpty()) {
                        User user = Entity2Model.map(optionalUserEntity.get());
                        return Single.just(Optional.wrap(user));
                    }

                    return stores.owners()
                            .findCommunityByDomain(accountId, domain)
                            .flatMap(optionalCommunityEntity -> {
                                if (optionalCommunityEntity.nonEmpty()) {
                                    Community community = Entity2Model.buildCommunityFromDbo(optionalCommunityEntity.get());
                                    return Single.just(Optional.<Owner>wrap(community));
                                }

                                return Single.just(Optional.empty());
                            });
                })
                .flatMap(optionalOwner -> {
                    if (optionalOwner.nonEmpty()) {
                        return Single.just(optionalOwner);
                    }

                    return networker.vkDefault(accountId)
                            .utils()
                            .resolveScreenName(domain)
                            .flatMap(response -> {
                                if ("user".equals(response.type)) {
                                    int userId = Integer.parseInt(response.object_id);
                                    return ownersRepository.getBaseOwnerInfo(accountId, userId, IOwnersRepository.MODE_ANY)
                                            .map(Optional::wrap);
                                }

                                if ("group".equals(response.type)) {
                                    int ownerId = -Math.abs(Integer.parseInt(response.object_id));
                                    return ownersRepository.getBaseOwnerInfo(accountId, ownerId, IOwnersRepository.MODE_ANY)
                                            .map(Optional::wrap);
                                }

                                return Single.just(Optional.empty());
                            });
                });
    }

    @SuppressLint("UseSparseArrays")
    private Single<Map<Integer, FriendList>> findFriendListsByIds(int accountId, int userId, @NonNull Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return Single.just(Collections.emptyMap());
        }

        return stores.owners()
                .findFriendsListsByIds(accountId, userId, ids)
                .flatMap(map -> {
                    if (map.size() == ids.size()) {
                        Map<Integer, FriendList> data = new HashMap<>(map.size());

                        for (int id : ids) {
                            FriendListEntity dbo = map.get(id);
                            data.put(id, new FriendList(dbo.getId(), dbo.getName()));
                        }

                        return Single.just(data);
                    }

                    return networker.vkDefault(accountId)
                            .friends()
                            .getLists(userId, true)
                            .map(items -> listEmptyIfNull(items.getItems()))
                            .flatMap(dtos -> {
                                List<FriendListEntity> dbos = new ArrayList<>(dtos.size());

                                Map<Integer, FriendList> data = new HashMap<>(map.size());

                                for (VkApiFriendList dto : dtos) {
                                    dbos.add(new FriendListEntity(dto.id, dto.name));
                                }

                                for (int id : ids) {
                                    boolean found = false;

                                    for (VkApiFriendList dto : dtos) {
                                        if (dto.id == id) {
                                            data.put(id, Dto2Model.transform(dto));
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        map.put(id, new FriendListEntity(id, "UNKNOWN"));
                                    }
                                }

                                return stores.relativeship()
                                        .storeFriendsList(accountId, userId, dbos)
                                        .andThen(Single.just(data));
                            });
                });
    }

    @Override
    public Single<List<ShortLink>> getLastShortenedLinks(int accountId, Integer count, Integer offset) {
        return networker.vkDefault(accountId)
                .utils()
                .getLastShortenedLinks(count, offset)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<ShortLink> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<ShortLink> getShortLink(int accountId, String url, Integer t_private) {
        return networker.vkDefault(accountId)
                .utils()
                .getShortLink(url, t_private)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<Integer> deleteFromLastShortened(int accountId, String key) {
        return networker.vkDefault(accountId)
                .utils()
                .deleteFromLastShortened(key)
                .map(out -> out);
    }

    @Override
    public Single<VKApiCheckedLink> checkLink(int accountId, String url) {
        return networker.vkDefault(accountId)
                .utils()
                .checkLink(url)
                .map(out -> out);
    }

    @Override
    public Single<VkApiChatResponse> joinChatByInviteLink(int accountId, String link) {
        return networker.vkDefault(accountId)
                .utils()
                .joinChatByInviteLink(link)
                .map(out -> out);
    }

    @Override
    public Single<VkApiLinkResponse> getInviteLink(int accountId, Integer peer_id, Integer reset) {
        return networker.vkDefault(accountId)
                .utils()
                .getInviteLink(peer_id, reset)
                .map(out -> out);
    }

    @Override
    public Single<Integer> customScript(int accountId, String code) {
        return networker.vkDefault(accountId)
                .utils()
                .customScript(code)
                .map(out -> out);
    }
}
