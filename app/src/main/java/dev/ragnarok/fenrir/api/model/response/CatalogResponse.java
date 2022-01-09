package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.VKApiCatalogLink;
import dev.ragnarok.fenrir.api.model.VKApiVideo;


public class CatalogResponse {

    @SerializedName("audios")
    public List<VKApiAudio> audios;

    @SerializedName("playlists")
    public List<VKApiAudioPlaylist> playlists;

    @SerializedName("videos")
    public List<VKApiVideo> videos;

    @SerializedName("items")
    public List<VKApiCatalogLink> items;

    @SerializedName("next_from")
    public String nextFrom;
}
