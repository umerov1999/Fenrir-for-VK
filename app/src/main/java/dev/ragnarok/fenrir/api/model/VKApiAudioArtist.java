package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VKApiAudioArtist implements VKApiAttachment {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("photo")
    public List<Image> photo;

    public VKApiAudioArtist() {

    }

    @Override
    public String getType() {
        return TYPE_ARTIST;
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
