package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class Market extends AbsModel {

    public static final Creator<Market> CREATOR = new Creator<Market>() {
        @Override
        public Market createFromParcel(Parcel in) {
            return new Market(in);
        }

        @Override
        public Market[] newArray(int size) {
            return new Market[size];
        }
    };

    private final int id;
    private final int owner_id;
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

    public Market(int id, int owner_id) {
        this.id = id;
        this.owner_id = owner_id;
    }

    protected Market(Parcel in) {
        super(in);
        id = in.readInt();
        owner_id = in.readInt();
        access_key = in.readString();
        is_favorite = in.readByte() != 0;
        weight = in.readInt();
        availability = in.readInt();
        date = in.readInt();
        title = in.readString();
        description = in.readString();
        price = in.readString();
        dimensions = in.readString();
        thumb_photo = in.readString();
        sku = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeString(access_key);
        dest.writeByte(is_favorite ? (byte) 1 : (byte) 0);
        dest.writeInt(weight);
        dest.writeInt(availability);
        dest.writeInt(date);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(dimensions);
        dest.writeString(thumb_photo);
        dest.writeString(sku);
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

    public Market setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public boolean isIs_favorite() {
        return is_favorite;
    }

    public Market setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public Market setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public int getAvailability() {
        return availability;
    }

    public Market setAvailability(int availability) {
        this.availability = availability;
        return this;
    }

    public int getDate() {
        return date;
    }

    public Market setDate(int date) {
        this.date = date;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Market setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Market setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public Market setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getDimensions() {
        return dimensions;
    }

    public Market setDimensions(String dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    public String getThumb_photo() {
        return thumb_photo;
    }

    public Market setThumb_photo(String thumb_photo) {
        this.thumb_photo = thumb_photo;
        return this;
    }

    public String getSku() {
        return sku;
    }

    public Market setSku(String sku) {
        this.sku = sku;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
