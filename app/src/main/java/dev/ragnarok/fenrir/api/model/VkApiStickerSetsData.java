package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;


public class VKApiStickerSetsData {

    @Nullable
    @SerializedName("recent")
    public Items<VKApiSticker> recent;

    @Nullable
    @SerializedName("sticker_pack")
    public Items<VKApiStickerSet.Product> sticker_pack;

}
