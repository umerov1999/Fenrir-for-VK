package dev.ragnarok.fenrir.db.interfaces;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.StickerEntity;
import dev.ragnarok.fenrir.db.model.entity.StickerSetEntity;
import dev.ragnarok.fenrir.db.model.entity.StickersKeywordsEntity;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IStickersStorage extends IStorage {

    Completable store(int accountId, List<StickerSetEntity> sets);

    Completable storeKeyWords(int accountId, List<StickersKeywordsEntity> sets);

    Single<List<StickerSetEntity>> getPurchasedAndActive(int accountId);

    Single<List<StickerEntity>> getKeywordsStickers(int accountId, String s);
}