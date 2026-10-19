package dev.ragnarok.fenrir.domain.impl

import android.content.Context
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.Dictionary
import dev.ragnarok.fenrir.api.model.VKApiStickerNotFull
import dev.ragnarok.fenrir.api.model.VKApiStickerSet.Product
import dev.ragnarok.fenrir.api.model.VKApiStickersKeywords
import dev.ragnarok.fenrir.api.model.response.HasNewStickersResponse
import dev.ragnarok.fenrir.db.interfaces.IStickersStorage
import dev.ragnarok.fenrir.db.model.entity.StickerSetEntity
import dev.ragnarok.fenrir.db.model.entity.StickersKeywordsEntity
import dev.ragnarok.fenrir.domain.interfaces.IStickersInteractor
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapSticker
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapStickerSet
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.map
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAllMutable
import dev.ragnarok.fenrir.model.Sticker
import dev.ragnarok.fenrir.model.Sticker.LocalSticker
import dev.ragnarok.fenrir.model.StickerSet
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadStoragePermissionSimple
import dev.ragnarok.fenrir.util.Utils.getCachedMyStickers
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNullMutable
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayedFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.emptyTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import java.io.File

class StickersInteractor(private val networker: INetworker, private val storage: IStickersStorage) :
    IStickersInteractor {
    override fun receiveAndStoreCustomStickerSets(accountId: Long): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .store().recentStickers
            .flatMapConcat { items ->
                val temp = StickerSetEntity(-1).setTitle("recent")
                    .setStickers(mapAll(listEmptyIfNull(listEmptyIfNull(items.items))) {
                        mapSticker(
                            it
                        )
                    }).setActive(true).setPurchased(true)
                if (items.items.isNullOrEmpty()) {
                    Settings.get().main().del_last_sticker_sets_custom_sync(accountId)
                }
                storage.storeStickerSetsCustom(accountId, listOf(temp))
            }
    }

    private suspend fun storeStickerKeyword(
        accountId: Long,
        dbo: Dictionary<VKApiStickersKeywords>,
        listKeys: MutableSet<Int>,
        clear: Boolean
    ): Boolean {
        val list: MutableList<VKApiStickersKeywords> =
            listEmptyIfNullMutable(dbo.dictionary)
        val temp: MutableList<StickersKeywordsEntity> = ArrayList()
        for (i in list) {
            val stickers = ArrayList<VKApiStickerNotFull>(i.stickers?.size.orZero())
            val keywords = ArrayList<String>(i.words?.size.orZero())
            for (s in i.stickers.orEmpty()) {
                s?.let {
                    if (listKeys.contains(it.packId)) {
                        stickers.add(it)
                    }
                }
            }
            for (s in i.words.orEmpty()) {
                s.nonNullNoEmpty {
                    keywords.add(it)
                }
            }
            if (keywords.isNotEmpty() && stickers.isNotEmpty()) {
                temp.add(StickersKeywordsEntity(keywords, stickers))
            }
        }
        return storage.storeKeyWords(accountId, temp, clear).single()
    }

    override fun receiveAndStoreStickerSets(accountId: Long): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .store()
            .stickersSets
            .flatMapConcat { items ->
                val list: MutableList<Product> = listEmptyIfNullMutable(items.items)
                val listKeys: MutableSet<Int> = HashSet(list.size)
                for (i in list) {
                    listKeys.add(i.id)
                }
                if (Settings.get().ui().isStickers_by_new) {
                    list.reverse()
                }
                val ret = mapAllMutable(list) { mapStickerSet(it) }
                if (list.isEmpty()) {
                    Settings.get().main().del_last_sticker_sets_sync(accountId)
                }
                storage.storeStickerSets(accountId, ret).flatMapConcat {
                    if (Settings.get().main().isHint_stickers) {
                        val first = networker.vkDefault(accountId)
                            .store().getStickerKeywords(0, null).single()
                        storeStickerKeyword(accountId, first, listKeys, true)
                        for (i in 1 until first.chunksCount) {
                            val s = networker.vkDefault(accountId)
                                .store().getStickerKeywords(i, first.chunksHash).delayedFlow(500)
                                .single()
                            storeStickerKeyword(accountId, s, listKeys, false)
                        }
                    }
                    toFlow(true)
                }
            }
    }

    override fun getStickerSets(accountId: Long): Flow<List<StickerSet>> {
        return storage.getStickerSets(accountId)
            .map { entities ->
                mapAll(entities) {
                    map(
                        it
                    )
                }
            }
    }

    override fun hasNewStickers(accountId: Long): Flow<HasNewStickersResponse> {
        return networker.vkDefault(accountId)
            .store()
            .hasNewStickers
    }

    override fun getKeywordsStickers(accountId: Long, s: String): Flow<List<Sticker>> {
        return storage.getKeywordsStickers(accountId, s)
            .flatMapConcat {
                if (it.isEmpty()) {
                    toFlow(emptyList())
                } else {
                    storage.getStickerSets(accountId)
                        .map { entities ->
                            val models = mapAll(entities) { dbo ->
                                map(
                                    dbo
                                )
                            }
                            val ret = ArrayList<Sticker>(it.size)
                            for (i in it) {
                                var found = false
                                for (s in models) {
                                    if (s.id == i.packId) {
                                        for (l in s.stickers.orEmpty()) {
                                            if (l.id == i.stickerId) {
                                                ret.add(l)
                                                found = true
                                                break
                                            }
                                        }
                                    }
                                    if (found) {
                                        break
                                    }
                                }
                            }
                            ret
                            /*
                            networker.vkDefault(accountId)
                                .store().getStickers(it).map { res ->
                                    mapAll(res) { st ->
                                        Dto2Model.transform(st)
                                    }
                                }
                             */
                        }
                }
            }
    }

    override fun placeToStickerCache(context: Context): Flow<Boolean> {
        return if (!hasReadStoragePermissionSimple(context)) {
            emptyTaskFlow()
        } else {
            flow {
                val temp = File(Settings.get().main().stickerDir)
                if (!temp.exists()) {
                    emit(false)
                } else {
                    val file_list = temp.listFiles()
                    if (file_list == null || file_list.isEmpty()) {
                        emit(false)
                    } else {
                        file_list.sortBy {
                            it.lastModified()
                        }
                        getCachedMyStickers().clear()
                        for (u in file_list) {
                            if (u.isFile && (u.name.contains(".png") || u.name.contains(".webp"))) {
                                getCachedMyStickers().add(LocalSticker(u.absolutePath, false))
                            } else if (u.isFile && u.name.contains(".json")) {
                                getCachedMyStickers().add(LocalSticker(u.absolutePath, true))
                            }
                        }
                        emit(true)
                    }
                }
            }
        }
    }
}