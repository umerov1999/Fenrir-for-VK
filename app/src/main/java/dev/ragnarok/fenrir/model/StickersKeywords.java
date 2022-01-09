package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public class StickersKeywords implements Parcelable {

    public static final Parcelable.Creator<StickersKeywords> CREATOR = new Parcelable.Creator<StickersKeywords>() {
        @Override
        public StickersKeywords createFromParcel(Parcel in) {
            return new StickersKeywords(in);
        }

        @Override
        public StickersKeywords[] newArray(int size) {
            return new StickersKeywords[size];
        }
    };
    private final List<String> keywords;
    private final List<Sticker> stickers;

    StickersKeywords(Parcel in) {
        keywords = new ArrayList<>();
        in.readStringList(keywords);
        stickers = in.createTypedArrayList(Sticker.CREATOR);
    }

    public StickersKeywords(List<String> keywords, List<Sticker> stickers) {
        this.keywords = keywords;
        this.stickers = stickers;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(keywords);
        dest.writeTypedList(stickers);
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
