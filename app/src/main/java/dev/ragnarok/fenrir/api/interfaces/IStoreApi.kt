package dev.ragnarok.fenrir.api.interfaces

import dev.ragnarok.fenrir.api.model.VkApiStickerSetsData
import dev.ragnarok.fenrir.api.model.VkApiStickersKeywords
import io.reactivex.rxjava3.core.Single

interface IStoreApi {
    val stickerKeywords: Single<VkApiStickersKeywords>
    val stickers: Single<VkApiStickerSetsData>
}