package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class VKApiStickersKeywords {

    @Nullable
    @SerializedName("words_stickers")
    public List<List<VKApiSticker>> words_stickers;

    @Nullable
    @SerializedName("keywords")
    public List<List<String>> keywords;

}
