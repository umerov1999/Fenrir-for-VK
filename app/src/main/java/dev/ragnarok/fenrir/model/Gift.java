package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class Gift extends AbsModel {
    public static final Creator<Gift> CREATOR = new Creator<Gift>() {
        @Override
        public Gift createFromParcel(Parcel in) {
            return new Gift(in);
        }

        @Override
        public Gift[] newArray(int size) {
            return new Gift[size];
        }
    };
    private int id;
    private int fromId;
    private String message;
    private long date;
    private GiftItem giftItem;
    private int privacy;

    public Gift(Parcel in) {
        super(in);
        id = in.readInt();
        fromId = in.readInt();
        message = in.readString();
        date = in.readLong();
        giftItem = in.readParcelable(GiftItem.class.getClassLoader());
        privacy = in.readInt();
    }

    public Gift(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Gift setId(int id) {
        this.id = id;
        return this;
    }

    public int getFromId() {
        return fromId;
    }

    public Gift setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public String getThumb() {
        if (giftItem != null) {
            return giftItem.getThumb();
        }
        return null;
    }

    public String getMessage() {
        return message;
    }

    public Gift setMessage(String message) {
        this.message = message;
        return this;
    }

    public long getDate() {
        return date;
    }

    public Gift setDate(long date) {
        this.date = date;
        return this;
    }

    public GiftItem getGiftItem() {
        return giftItem;
    }

    public Gift setGiftItem(GiftItem giftItem) {
        this.giftItem = giftItem;
        return this;
    }

    public int getPrivacy() {
        return privacy;
    }

    public Gift setPrivacy(int privacy) {
        this.privacy = privacy;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeInt(id);
        parcel.writeInt(fromId);
        parcel.writeString(message);
        parcel.writeLong(date);
        parcel.writeParcelable(giftItem, flags);
        parcel.writeInt(privacy);
    }
}
