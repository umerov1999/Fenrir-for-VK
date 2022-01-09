package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VkApiCover {

    @SerializedName("enabled")
    public boolean enabled;

    @SerializedName("images")
    public List<Image> images;

    public static final class Image {

        @SerializedName("url")
        public String url;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;
    }
}