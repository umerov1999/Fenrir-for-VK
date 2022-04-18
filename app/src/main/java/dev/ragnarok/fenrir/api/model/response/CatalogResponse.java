package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.VKApiCatalogLink;
import dev.ragnarok.fenrir.api.model.VKApiVideo;


public class CatalogResponse {

    @Nullable
    @SerializedName("audios")
    public List<VKApiAudio> audios;

    @Nullable
    @SerializedName("playlists")
    public List<VKApiAudioPlaylist> playlists;

    @Nullable
    @SerializedName("videos")
    public List<VKApiVideo> videos;

    @Nullable
    @SerializedName("items")
    public List<VKApiCatalogLink> items;

    @Nullable
    @SerializedName("next_from")
    public String nextFrom;
}
