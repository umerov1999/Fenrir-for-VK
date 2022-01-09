package dev.ragnarok.fenrir.db.model.entity;

import java.util.List;


public final class StickersKeywordsEntity {
    private final List<String> keywords;
    private final List<StickerEntity> stickers;

    public StickersKeywordsEntity(List<String> keywords, List<StickerEntity> stickers) {
        this.keywords = keywords;
        this.stickers = stickers;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<StickerEntity> getStickers() {
        return stickers;
    }
}
