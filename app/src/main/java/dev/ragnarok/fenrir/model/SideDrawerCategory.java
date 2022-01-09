package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StringRes;

public class SideDrawerCategory implements Parcelable {

    public static Creator<SideDrawerCategory> CREATOR = new Creator<SideDrawerCategory>() {
        public SideDrawerCategory createFromParcel(Parcel source) {
            return new SideDrawerCategory(source);
        }

        public SideDrawerCategory[] newArray(int size) {
            return new SideDrawerCategory[size];
        }
    };
    @StringRes
    private final int title;
    @SideSwitchableCategory
    private final int key;
    private boolean checked;

    public SideDrawerCategory(@SwitchableCategory int key, @StringRes int title) {
        this.title = title;
        this.key = key;
    }

    public SideDrawerCategory(Parcel in) {
        title = in.readInt();
        //noinspection ResourceType
        key = in.readInt();
        checked = in.readInt() == 1;
    }

    @StringRes
    public int getTitle() {
        return title;
    }

    @SideSwitchableCategory
    public int getKey() {
        return key;
    }

    public boolean isChecked() {
        return checked;
    }

    public SideDrawerCategory setChecked(boolean checked) {
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
