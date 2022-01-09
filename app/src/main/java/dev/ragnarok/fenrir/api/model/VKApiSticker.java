package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Describes a photo object from VK.
 */
public class VKApiSticker implements VKApiAttachment {

    /**
     * Sticker ID, positive number
     */
    public int sticker_id;

    @SerializedName("images")
    public List<Image> images;

    @SerializedName("images_with_background")
    public List<Image> images_with_background;

    @SerializedName("animation_url")
    public String animation_url;

    @SerializedName("animations")
    public List<VKApiAnimation> animations;

    @Override
    public String getType() {
        return TYPE_STICKER;
    }

    public static final class Image {
        @SerializedName("url")
        public String url;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;
    }

    public static final class VKApiAnimation {
        @SerializedName("type")
        public String type;

        @SerializedName("url")
        public String url;
    }
}