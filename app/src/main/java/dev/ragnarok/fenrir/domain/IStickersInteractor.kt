package dev.ragnarok.fenrir.domain

import android.content.Context
import dev.ragnarok.fenrir.model.Sticker
import dev.ragnarok.fenrir.model.StickerSet
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IStickersInteractor {
    fun getAndStore(accountId: Int): Completable
    fun getStickers(accountId: Int): Single<List<StickerSet>>
    fun getKeywordsStickers(accountId: Int, s: String?): Single<List<Sticker>>
    fun PlaceToStickerCache(context: Context): Completable
}