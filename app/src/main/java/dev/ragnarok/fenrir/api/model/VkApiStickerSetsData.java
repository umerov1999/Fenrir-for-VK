package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;


public class VkApiStickerSetsData {

    @SerializedName("recent")
    public Items<VKApiSticker> recent;

    @SerializedName("sticker_pack")
    public Items<VKApiStickerSet.Product> sticker_pack;

}
