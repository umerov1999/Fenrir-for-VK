package dev.ragnarok.fenrir.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class LocalPhoto implements Parcelable, Comparable<LocalPhoto>, ISelectable {

    public static final Creator<LocalPhoto> CREATOR = new Creator<LocalPhoto>() {

        @Override
        public LocalPhoto createFromParcel(Parcel s) {
            return new LocalPhoto(s);
        }

        @Override
        public LocalPhoto[] newArray(int size) {
            return new LocalPhoto[size];
        }
    };
    private long imageId;
    private Uri fullImageUri;
    private boolean selected;
    private int index;

    public LocalPhoto() {
    }

    public LocalPhoto(Parcel in) {
        imageId = in.readLong();
        fullImageUri = Uri.parse(in.readString());
        selected = in.readInt() == 1;
        index = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(imageId);
        dest.writeString(fullImageUri.toString());
        dest.writeInt(selected ? 1 : 0);
        dest.writeInt(index);
    }

    public long getImageId() {
        return imageId;
    }

    public LocalPhoto setImageId(long imageId) {
        this.imageId = imageId;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public LocalPhoto setIndex(int index) {
        this.index = index;
        return this;
    }

    public Uri getFullImageUri() {
        return fullImageUri;
    }

    public LocalPhoto setFullImageUri(Uri fullImageUri) {
        this.fullImageUri = fullImageUri;
        return this;
    }

    @Override
    public int compareTo(@NonNull LocalPhoto another) {
        return index - another.index;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public LocalPhoto setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }
}
