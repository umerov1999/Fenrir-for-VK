package dev.ragnarok.fenrir.api.model;


import androidx.annotation.NonNull;

public class VKApiMarket implements VKApiAttachment {

    public int id;

    public int owner_id;

    public String access_key;

    public int weight;

    public int availability;

    public int date;

    public boolean is_favorite;

    public String title;

    public String description;

    public String price;

    public String dimensions;

    public String thumb_photo;

    public String sku;

    @NonNull
    @Override
    public String getType() {
        return TYPE_MARKET;
    }

}
