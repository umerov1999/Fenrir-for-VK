package dev.ragnarok.fenrir.fragment.search.nextfrom;

import android.os.Parcel;

public class IntNextFrom extends AbsNextFrom {

    public static final Creator<IntNextFrom> CREATOR = new Creator<IntNextFrom>() {
        @Override
        public IntNextFrom createFromParcel(Parcel in) {
            return new IntNextFrom(in);
        }

        @Override
        public IntNextFrom[] newArray(int size) {
            return new IntNextFrom[size];
        }
    };
    private int offset;

    public IntNextFrom(int initValue) {
        offset = initValue;
    }

    protected IntNextFrom(Parcel in) {
        offset = in.readInt();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(offset);
    }

    @Override
    public void reset() {
        offset = 0;
    }
}
