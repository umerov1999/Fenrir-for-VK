package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

import dev.ragnarok.fenrir.util.ParcelUtils;

public final class Icon implements Parcelable {

    public static final Creator<Icon> CREATOR = new Creator<Icon>() {
        @Override
        public Icon createFromParcel(Parcel in) {
            return new Icon(in);
        }

        @Override
        public Icon[] newArray(int size) {
            return new Icon[size];
        }
    };
    @DrawableRes
    private final Integer res;
    private final String url;

    private Icon(Integer res, String url) {
        this.res = res;
        this.url = url;
    }

    private Icon(Parcel in) {
        res = ParcelUtils.readObjectInteger(in);
        url = in.readString();
    }

    public static Icon fromUrl(String url) {
        return new Icon(null, url);
    }

    public static Icon fromResources(@DrawableRes int res) {
        return new Icon(res, null);
    }

    public boolean isRemote() {
        return url != null;
    }

    public Integer getRes() {
        return res;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeObjectInteger(dest, res);
        dest.writeString(url);
    }
}