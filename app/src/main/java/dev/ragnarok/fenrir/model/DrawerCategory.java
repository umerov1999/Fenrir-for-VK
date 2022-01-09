package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StringRes;

public class DrawerCategory implements Parcelable {

    public static Creator<DrawerCategory> CREATOR = new Creator<DrawerCategory>() {
        public DrawerCategory createFromParcel(Parcel source) {
            return new DrawerCategory(source);
        }

        public DrawerCategory[] newArray(int size) {
            return new DrawerCategory[size];
        }
    };
    @StringRes
    private final int title;
    @SwitchableCategory
    private final int key;
    private boolean checked;

    public DrawerCategory(@SwitchableCategory int key, @StringRes int title) {
        this.title = title;
        this.key = key;
    }

    public DrawerCategory(Parcel in) {
        title = in.readInt();
        //noinspection ResourceType
        key = in.readInt();
        checked = in.readInt() == 1;
    }

    @StringRes
    public int getTitle() {
        return title;
    }

    @SwitchableCategory
    public int getKey() {
        return key;
    }

    public boolean isChecked() {
        return checked;
    }

    public DrawerCategory setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(title);
        dest.writeInt(key);
        dest.writeInt(checked ? 1 : 0);
    }
}
