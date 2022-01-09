package dev.ragnarok.fenrir.db.model.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class StickerEntity extends Entity {

    @SerializedName("id")
    private int id;
    @SerializedName("images")
    private List<Img> images;
    @SerializedName("imagesWithBackground")
    private List<Img> imagesWithBackground;
    @SerializedName("animations")
    private List<AnimationEntity> animations;
    @SerializedName("animationUrl")
    private String animationUrl;

    public int getId() {
        return id;
    }

    public StickerEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getAnimationUrl() {
        return animationUrl;
    }

    public StickerEntity setAnimationUrl(String animationUrl) {
        this.animationUrl = animationUrl;
        return this;
    }

    public List<Img> getImages() {
        return images;
    }

    public StickerEntity setImages(List<Img> images) {
        this.images = images;
        return this;
    }

    public List<AnimationEntity> getAnimations() {
        return animations;
    }

    public StickerEntity setAnimations(List<AnimationEntity> animations) {
        this.animations = animations;
        return this;
    }

    public List<Img> getImagesWithBackground() {
        return imagesWithBackground;
    }

    public StickerEntity setImagesWithBackground(List<Img> imagesWithBackground) {
        this.imagesWithBackground = imagesWithBackground;
        return this;
    }

    public static final class AnimationEntity {
        @SerializedName("type")
        private String type;
        @SerializedName("url")
        private String url;

        public AnimationEntity set(String url, String type) {
            this.url = url;
            this.type = type;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }
    }

    public static final class Img {

        @SerializedName("url")
        private String url;
        @SerializedName("width")
        private int width;
        @SerializedName("height")
        private int height;

        public Img set(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
            return this;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getUrl() {
            return url;
        }
    }
}