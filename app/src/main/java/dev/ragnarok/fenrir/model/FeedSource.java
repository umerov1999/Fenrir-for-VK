package dev.ragnarok.fenrir.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.adapter.horizontal.Entry;

public class FeedSource implements Entry, Parcelable {

    public static final Creator<FeedSource> CREATOR = new Creator<FeedSource>() {
        @Override
        public FeedSource createFromParcel(Parcel in) {
            return new FeedSource(in);
        }

        @Override
        public FeedSource[] newArray(int size) {
            return new FeedSource[size];
        }
    };
    private final String value;
    private final Text title;
    private boolean active;
    private boolean custom;

    public FeedSource(String value, String title, boolean custom) {
        this.value = value;
        this.title = new Text(title);
        this.custom = custom;
    }

    public FeedSource(String value, @StringRes int title, boolean custom) {
        this.value = value;
        this.title = new Text(title);
        this.custom = custom;
    }

    protected FeedSource(Parcel in) {
        value = in.readString();
        title = in.readParcelable(Text.class.getClassLoader());
        active = in.readByte() != 0;
        custom = in.readByte() != 0;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(value);
        dest.writeParcelable(title, flags);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeByte((byte) (custom ? 1 : 0));
    }

    @Override
    public String getTitle(@NonNull Context context) {
        return title.getText(context);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public FeedSource setActive(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public boolean isCustom() {
        return custom;
    }

    public FeedSource setCustom(boolean custom) {
        this.custom = custom;
        return this;
    }
}
