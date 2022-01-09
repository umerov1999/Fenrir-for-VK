package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import dev.ragnarok.fenrir.util.Utils;

public class GiftItem extends AbsModel {
    public static final Creator<GiftItem> CREATOR = new Creator<GiftItem>() {
        @Override
        public GiftItem createFromParcel(Parcel in) {
            return new GiftItem(in);
        }

        @Override
        public GiftItem[] newArray(int size) {
            return new GiftItem[size];
        }
    };
    private final int id;
    private String thumb256;
    private String thumb96;
    private String thumb48;

    public GiftItem(Parcel in) {
        id = in.readInt();
        thumb256 = in.readString();
        thumb96 = in.readString();
        thumb48 = in.readString();
    }

    public GiftItem(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getThumb() {
        return Utils.firstNonEmptyString(thumb256, thumb96, thumb48);
    }

    public String getThumb256() {
        return thumb256;
    }

    public GiftItem setThumb256(String thumb256) {
        this.thumb256 = thumb256;
        return this;
    }

    public String getThumb96() {
        return thumb96;
    }

    public GiftItem setThumb96(String thumb96) {
        this.thumb96 = thumb96;
        return this;
    }

    public String getThumb48() {
        return thumb48;
    }

    public GiftItem setThumb48(String thumb48) {
        this.thumb48 = thumb48;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(id);
        parcel.writeString(thumb256);
        parcel.writeString(thumb96);
        parcel.writeString(thumb48);
    }
}
