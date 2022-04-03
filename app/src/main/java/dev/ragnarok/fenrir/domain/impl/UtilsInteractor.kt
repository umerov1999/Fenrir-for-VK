package dev.ragnarok.fenrir.domain.impl

import android.annotation.SuppressLint
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiCheckedLink
import dev.ragnarok.fenrir.api.model.VKApiShortLink
import dev.ragnarok.fenrir.api.model.VkApiFriendList
import dev.ragnarok.fenrir.api.model.response.ResolveDomailResponse
import dev.ragnarok.fenrir.api.model.response.VkApiChatResponse
import dev.ragnarok.fenrir.api.model.response.VkApiLinkResponse
import dev.ragnarok.fenrir.db.interfaces.IStorages
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity
import dev.ragnarok.fenrir.db.model.entity.FriendListEntity
import dev.ragnarok.fenrir.db.model.entity.UserEntity
import dev.ragnarok.fenrir.domain.IOwnersRepository
import dev.ragnarok.fenrir.domain.IUtilsInteractor
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transform
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.buildCommunityFromDbo
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.map
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.empty
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Single
import kotlin.math.abs

class UtilsInteractor(
    private val networker: INetworker,
    private val stores: IStorages,
    private val ownersRepository: IOwnersRepository
) : IUtilsInteractor {
    @SuppressLint("UseSparseArrays")
    override fun createFullPrivacies(
        accountId: Int,
        orig: Map<Int, SimplePrivacy>
    ): Single<Map<Int, Privacy>> {
        return Single.just(orig)
            .flatMap {
                val uids: MutableSet<Int> = HashSet()
                val listsIds: MutableSet<Int> = HashSet()
                for ((_, privacy) in orig) {
                    if (privacy.entries.isNullOrEmpty()) {
                        continue
                    }
                    for (entry in privacy.entries) {
                        when (entry.type) {
                            SimplePrivacy.Entry.TYPE_FRIENDS_LIST -> listsIds.add(entry.id)
                            SimplePrivacy.Entry.TYPE_USER -> uids.add(entry.id)
                        }
                    }
                }
                ownersRepository.findBaseOwnersDataAsBundle(
                    accountId,
                    uids,
                    IOwnersRepository.MODE_ANY
                )
                    .flatMap { owners: IOwnersBundle ->
                        findFriendListsByIds(accountId, accountId, listsIds)
                            .map { lists: Map<Int, FriendList> ->
                                val privacies: MutableMap<Int, Privacy> = HashMap(
                                    safeCountOf(orig)
                                )
                                for ((key, value) in orig) {
                                    val full: Privacy = transform(value, owners, lists)
                                    privacies[key] = full
                                }
                                privacies
                            }
                    }
            }
    }

    override fun resolveDomain(accountId: Int, domain: String?): Single<Optional<Owner>> {
        return stores.owners()
            .findUserByDomain(accountId, domain)
            .flatMap<Optional<Owner>> { optionalUserEntity: Optional<UserEntity> ->
                if (optionalUserEntity.nonEmpty()) {
                    val user = map(optionalUserEntity.get())
                    return@flatMap Single.just(wrap<Owner>(user))
                }
                stores.owners()
                    .findCommunityByDomain(accountId, domain)
                    .flatMap { optionalCommunityEntity: Optional<CommunityEntity> ->
                        if (optionalCommunityEntity.nonEmpty()) {
                            val community =
                                buildCommunityFromDbo(optionalCommunityEntity.requareNonEmpty())
                            Single.just(wrap(community))
                        } else {
                            Single.just(empty())
                        }
                    }
            }
            .flatMap { optionalOwner: Optional<Owner> ->
                if (optionalOwner.nonEmpty()) {
                    return@flatMap Single.just(optionalOwner)
                }
                networker.vkDefault(accountId)
                    .utils()
                    .resolveScreenName(domain)
                    .flatMap { response: ResolveDomailResponse ->
                        if ("user" == response.type) {
                            val userId = response.object_id.toInt()
                            ownersRepository.getBaseOwnerInfo(
                                accountId,
                                userId,
                                IOwnersRepository.MODE_ANY
                            )
                                .map { wrap(it) }
                        } else if ("group" == response.type) {
                            val ownerId = -abs(response.object_id.toInt())
                            ownersRepository.getBaseOwnerInfo(
                                accountId,
                                ownerId,
                                IOwnersRepository.MODE_ANY
                            )
                                .map { wrap(it) }
                        }
                        Single.just(empty())
                    }
            }
    }

    @SuppressLint("UseSparseArrays")
    private fun findFriendListsByIds(
        accountId: Int,
        userId: Int,
        ids: Collection<Int>
    ): Single<Map<Int, FriendList>> {
        return if (ids.isEmpty()) {
            Single.just(emptyMap())
        } else stores.owners()
            .findFriendsListsByIds(accountId, userId, ids)
            .flatMap { map: MutableMap<Int, FriendListEntity> ->
                if (map.size == ids.size) {
                    val data: MutableMap<Int, FriendList> = HashMap(map.size)
                    for (id in ids) {
                        val dbo = map[id] ?: continue
                        data[id] = FriendList(dbo.id, dbo.name)
                    }
                    return@flatMap Single.just(data)
                }
                networker.vkDefault(accountId)
                    .friends()
                    .getLists(userId, true)
                    .map { items: Items<VkApiFriendList>? ->
                        listEmptyIfNull<VkApiFriendList>(
                            items?.getItems()
                        )
                    }
                    .flatMap { dtos: List<VkApiFriendList> ->
                        val dbos: MutableList<FriendListEntity> = ArrayList(dtos.size)
                        val data: MutableMap<Int, FriendList> = HashMap(map.size)
                        for (dto in dtos) {
                            dbos.add(FriendListEntity(dto.id, dto.name))
                        }
                        for (id in ids) {
                            var found = false
                            for (dto in dtos) {
                                if (dto.id == id) {
                                    data[id] = transform(dto)
                                    found = true
                                    break
                                }
                            }
                            if (!found) {
                                map[id] = FriendListEntity(id, "UNKNOWN")
                            }
                        }
                        stores.relativeship()
                            .storeFriendsList(accountId, userId, dbos)
                            .andThen(Single.just(data))
                    }
            }
    }

    override fun getLastShortenedLinks(
        accountId: Int,
        count: Int?,
        offset: Int?
    ): Single<List<ShortLink>> {
        return networker.vkDefault(accountId)
            .utils()
            .getLastShortenedLinks(count, offset)
            .map { items: Items<VKApiShortLink>? ->
                listEmptyIfNull<VKApiShortLink>(
                    items?.getItems()
                )
            }
            .map { out: List<VKApiShortLink> ->
                val ret: MutableList<ShortLink> = ArrayList()
                for (i in out.indices) ret.add(transform(out[i]))
                ret
            }
    }

    override fun getShortLink(accountId: Int, url: String?, t_private: Int?): Single<ShortLink> {
        return networker.vkDefault(accountId)
            .utils()
            .getShortLink(url, t_private)
            .map { obj: VKApiShortLink -> transform(obj) }
    }

    override fun deleteFromLastShortened(accountId: Int, key: String?): Single<Int> {
        return networker.vkDefault(accountId)
            .utils()
            .deleteFromLastShortened(key)
            .map { out: Int -> out }
    }

    override fun checkLink(accountId: Int, url: String?): Single<VKApiCheckedLink> {
        return networker.vkDefault(accountId)
            .utils()
            .checkLink(url)
            .map { out: VKApiCheckedLink -> out }
    }

    override fun joinChatByInviteLink(accountId: Int, link: String?): Single<VkApiChatResponse> {
        return networker.vkDefault(accountId)
            .utils()
            .joinChatByInviteLink(link)
            .map { out: VkApiChatResponse -> out }
    }

    override fun getInviteLink(
        accountId: Int,
        peer_id: Int?,
        reset: Int?
    ): Single<VkApiLinkResponse> {
        return networker.vkDefault(accountId)
            .utils()
            .getInviteLink(peer_id, reset)
            .map { out: VkApiLinkResponse -> out }
    }

    override fun customScript(accountId: Int, code: String?): Single<Int> {
        return networker.vkDefault(accountId)
            .utils()
            .customScript(code)
            .map { out: Int -> out }
    }
}