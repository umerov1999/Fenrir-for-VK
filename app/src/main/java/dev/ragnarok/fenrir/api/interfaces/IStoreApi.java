package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.VkApiStickerSetsData;
import dev.ragnarok.fenrir.api.model.VkApiStickersKeywords;
import io.reactivex.rxjava3.core.Single;


public interface IStoreApi {
    Single<VkApiStickersKeywords> getStickerKeywords();

    Single<VkApiStickerSetsData> getStickers();
}