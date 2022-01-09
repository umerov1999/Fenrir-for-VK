package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class MarketEntity extends Entity {
    private int id;
    private int owner_id;
    private String access_key;
    private boolean is_favorite;
    private int weight;
    private int availability;
    private int date;
    private String title;
    private String description;
    private String price;
    private String dimensions;
    private String thumb_photo;
    private String sku;

    public MarketEntity set(int id, int owner_id) {
        this.id = id;
        this.owner_id = owner_id;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public String getAccess_key() {
        return access_key;
    }

    public MarketEntity setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public boolean isIs_favorite() {
        return is_favorite;
    }

    public MarketEntity setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public MarketEntity setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public int getAvailability() {
        return availability;
    }

    public MarketEntity setAvailability(int availability) {
        this.availability = availability;
        return this;
    }

    public int getDate() {
        return date;
    }

    public MarketEntity setDate(int date) {
        this.date = date;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MarketEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MarketEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public MarketEntity setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getDimensions() {
        return dimensions;
    }

    public MarketEntity setDimensions(String dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    public String getThumb_photo() {
        return thumb_photo;
    }

    public MarketEntity setThumb_photo(String thumb_photo) {
        this.thumb_photo = thumb_photo;
        return this;
    }

    public String getSku() {
        return sku;
    }

    public MarketEntity setSku(String sku) {
        this.sku = sku;
        return this;
    }
}
