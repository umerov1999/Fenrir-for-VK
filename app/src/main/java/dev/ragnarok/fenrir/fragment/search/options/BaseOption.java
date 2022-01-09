package dev.ragnarok.fenrir.fragment.search.options;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BaseOption implements Parcelable, Cloneable {

    public static final int DATABASE = 44;
    public static final int SIMPLE_BOOLEAN = 45;
    public static final int SIMPLE_TEXT = 46;
    public static final int SIMPLE_NUMBER = 47;
    public static final int SPINNER = 48;
    public static final int GPS = 49;
    public static final int DATE_TIME = 50;

    public static final int NO_DEPENDENCY = -1;
    public static final Creator<BaseOption> CREATOR = new Creator<BaseOption>() {
        @Override
        public BaseOption createFromParcel(Parcel in) {
            return new BaseOption(in);
        }

        @Override
        public BaseOption[] newArray(int size) {
            return new BaseOption[size];
        }
    };
    public final int key;
    public final int optionType;
    public final boolean active;
    public final int title;
    public int parentDependencyKey;
    public int[] childDependencies;

    public BaseOption(int optionType, int key, int title, boolean active) {
        this.optionType = optionType;
        this.key = key;
        this.title = title;
        this.active = active;
        parentDependencyKey = NO_DEPENDENCY;
    }

    protected BaseOption(Parcel in) {
        optionType = in.readInt();
        key = in.readInt();
        active = in.readByte() != 0;
        title = in.readInt();
        parentDependencyKey = in.readInt();
        childDependencies = in.createIntArray();
    }

    public void setChildDependencies(int... childs) {
        childDependencies = childs;
    }

    public void setDependencyOf(int key) {
        parentDependencyKey = key;
    }

    public void reset() {
        // must be implemented in child class
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(optionType);
        dest.writeInt(key);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeInt(title);
        dest.writeInt(parentDependencyKey);
        dest.writeIntArray(childDependencies);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseOption option = (BaseOption) o;

        if (optionType != option.optionType) return false;
        if (key != option.key) return false;
        return active == option.active;
    }

    @Override
    public int hashCode() {
        int result = optionType;
        result = 31 * result + key;
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @NonNull
    @Override
    public BaseOption clone() throws CloneNotSupportedException {
        BaseOption option = (BaseOption) super.clone();
        option.childDependencies = childDependencies == null ? null : childDependencies.clone();
        return option;
    }
}
