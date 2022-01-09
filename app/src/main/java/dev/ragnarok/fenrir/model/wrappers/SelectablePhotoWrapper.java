package dev.ragnarok.fenrir.model.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.ISelectable;
import dev.ragnarok.fenrir.model.Photo;

public class SelectablePhotoWrapper implements Parcelable, Comparable<SelectablePhotoWrapper>, ISelectable {

    public static final Creator<SelectablePhotoWrapper> CREATOR = new Creator<SelectablePhotoWrapper>() {
        @Override
        public SelectablePhotoWrapper createFromParcel(Parcel in) {
            return new SelectablePhotoWrapper(in);
        }

        @Override
        public SelectablePhotoWrapper[] newArray(int size) {
            return new SelectablePhotoWrapper[size];
        }
    };
    private final Photo photo;
    private boolean selected;
    private boolean downloaded;
    private int index;
    private boolean current;

    public SelectablePhotoWrapper(@NonNull Photo photo) {
        this.photo = photo;
    }

    protected SelectablePhotoWrapper(Parcel in) {
        photo = in.readParcelable(Photo.class.getClassLoader());
        selected = in.readByte() != 0;
        index = in.readInt();
        current = in.readByte() != 0;
        downloaded = in.readByte() != 0;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean getCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public SelectablePhotoWrapper setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
        return this;
    }

    @NonNull
    public Photo getPhoto() {
        return photo;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(photo, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeInt(index);
        dest.writeByte((byte) (current ? 1 : 0));
        dest.writeByte((byte) (downloaded ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectablePhotoWrapper that = (SelectablePhotoWrapper) o;
        return photo.equals(that.photo);

    }

    @Override
    public int hashCode() {
        return photo.hashCode();
    }

    @Override
    public int compareTo(@NonNull SelectablePhotoWrapper another) {
        return index - another.index;
    }
}
