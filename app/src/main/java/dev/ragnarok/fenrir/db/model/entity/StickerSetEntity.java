package dev.ragnarok.fenrir.db.model.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StickerSetEntity {

    private final int id;

    private List<Img> icon;

    private String title;

    private boolean purchased;

    private boolean promoted;

    private boolean active;

    private int position;

    private List<StickerEntity> stickers;

    public StickerSetEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<Img> getIcon() {
        return icon;
    }

    public StickerSetEntity setIcon(List<Img> icon) {
        this.icon = icon;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public StickerSetEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public StickerSetEntity setPurchased(boolean purchased) {
        this.purchased = purchased;
        return this;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public StickerSetEntity setPromoted(boolean promoted) {
        this.promoted = promoted;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public StickerSetEntity setPosition(int position) {
        this.position = position;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public StickerSetEntity setActive(boolean active) {
        this.active = active;
        return this;
    }

    public List<StickerEntity> getStickers() {
        return stickers;
    }

    public StickerSetEntity setStickers(List<StickerEntity> stickers) {
        this.stickers = stickers;
        return this;
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