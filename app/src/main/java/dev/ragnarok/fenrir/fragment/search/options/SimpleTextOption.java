package dev.ragnarok.fenrir.fragment.search.options;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

public class SimpleTextOption extends BaseOption {

    public static final Creator<SimpleTextOption> CREATOR = new Creator<SimpleTextOption>() {
        @Override
        public SimpleTextOption createFromParcel(Parcel in) {
            return new SimpleTextOption(in);
        }

        @Override
        public SimpleTextOption[] newArray(int size) {
            return new SimpleTextOption[size];
        }
    };
    public String value;

    public SimpleTextOption(int key, int title, boolean active) {
        super(SIMPLE_TEXT, key, title, active);
    }

    protected SimpleTextOption(Parcel in) {
        super(in);
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SimpleTextOption that = (SimpleTextOption) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public SimpleTextOption clone() throws CloneNotSupportedException {
        SimpleTextOption clone = (SimpleTextOption) super.clone();
        clone.value = value;
        return clone;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
