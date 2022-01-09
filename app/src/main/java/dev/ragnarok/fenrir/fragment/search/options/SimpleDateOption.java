package dev.ragnarok.fenrir.fragment.search.options;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class SimpleDateOption extends BaseOption {

    public static final Creator<SimpleDateOption> CREATOR = new Creator<SimpleDateOption>() {
        @Override
        public SimpleDateOption createFromParcel(Parcel in) {
            return new SimpleDateOption(in);
        }

        @Override
        public SimpleDateOption[] newArray(int size) {
            return new SimpleDateOption[size];
        }
    };
    public long timeUnix;

    public SimpleDateOption(int key, int title, boolean active) {
        super(DATE_TIME, key, title, active);
    }

    protected SimpleDateOption(Parcel in) {
        super(in);
        timeUnix = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(timeUnix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SimpleDateOption that = (SimpleDateOption) o;
        return timeUnix == that.timeUnix;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Long.valueOf(timeUnix).hashCode();
        return result;
    }

    @NonNull
    @Override
    public SimpleDateOption clone() throws CloneNotSupportedException {
        return (SimpleDateOption) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
