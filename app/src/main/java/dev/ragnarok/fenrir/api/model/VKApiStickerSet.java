package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VKApiStickerSet implements Identificable {
    @SerializedName("background")
    public String background;

    @SerializedName("description")
    public String description;

    @SerializedName("author")
    public String author;

    @SerializedName("free")
    public boolean free;

    @SerializedName("can_purchase")
    public boolean can_purchase;

    @SerializedName("payment_type")
    public String payment_type;

    @SerializedName("product")
    public Product product;

    @Override
    public int getId() {
        return product.id;
    }

    public static final class Image {
        @SerializedName("url")
        public String url;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;
    }

    public static class Product {

        @SerializedName("id")
        public int id;

        @SerializedName("purchased")
        public boolean purchased;

        @SerializedName("title")
        public String title;

        @SerializedName("promoted")
        public boolean promoted;

        @SerializedName("active")
        public boolean active;

        @SerializedName("type")
        public String type;

        @SerializedName("icon")
        public List<Image> icon;

        @SerializedName("stickers")
        public List<VKApiSticker> stickers;
    }
}