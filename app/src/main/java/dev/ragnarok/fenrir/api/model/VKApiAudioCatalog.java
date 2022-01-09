package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class VKApiAudioCatalog {

    @SerializedName("id")
    public String id;

    @SerializedName("source")
    public String source;

    @SerializedName("next_from")
    public String next_from;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("title")
    public String title;

    @SerializedName("type")
    public String type;

    @SerializedName("count")
    public int count;

    @SerializedName("audios")
    public List<VKApiAudio> audios;

    @SerializedName("playlists")
    public List<VKApiAudioPlaylist> playlists;

    @SerializedName("playlist")
    public VKApiAudioPlaylist playlist;

    @SerializedName("videos")
    public List<VKApiVideo> videos;

    @SerializedName("items")
    public List<VKApiCatalogLink> items;

    @SerializedName("artist")
    public VKApiArtistBlock artist;

    public static class VKApiArtistBlock {
        @SerializedName("name")
        public String name;
        @SerializedName("photo")
        public List<Image> images;
    }

    public static final class Image {
        @SerializedName("url")
        public String url;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;
    }
}
