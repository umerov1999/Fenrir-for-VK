package dev.ragnarok.fenrir.domain;

import android.content.Context;

import java.util.List;

import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.StickerSet;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IStickersInteractor {
    Completable getAndStore(int accountId);

    Single<List<StickerSet>> getStickers(int accountId);

    Single<List<Sticker>> getKeywordsStickers(int accountId, String s);

    Completable PlaceToStickerCache(Context context);
}