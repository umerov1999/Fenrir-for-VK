package dev.ragnarok.fenrir.util;

import android.os.Parcel;
import android.os.Parcelable;


public final class BooleanValue implements Parcelable {

    public static final Creator<BooleanValue> CREATOR = new Creator<BooleanValue>() {
        @Override
        public BooleanValue createFromParcel(Parcel in) {
            return new BooleanValue(in);
        }

        @Override
        public BooleanValue[] newArray(int size) {
            return new BooleanValue[size];
        }
    };
    private boolean value;

    public BooleanValue(boolean initialValue) {
        value = initialValue;
    }

    public BooleanValue() {
        this(false);
    }

    private BooleanValue(Parcel in) {
        value = in.readByte() != 0;
    }

    /**
     * @param value new boolean value
     * @return true if value was changed
     */
    public boolean setValue(boolean value) {
        if (this.value == value) {
            return false;
        }

        this.value = value;
        return true;
    }

    public boolean get() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (value ? 1 : 0));
    }
}
