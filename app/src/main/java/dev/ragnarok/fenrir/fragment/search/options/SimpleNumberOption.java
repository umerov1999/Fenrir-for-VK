package dev.ragnarok.fenrir.fragment.search.options;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

import dev.ragnarok.fenrir.util.ParcelUtils;

public class SimpleNumberOption extends BaseOption {

    public static final Creator<SimpleNumberOption> CREATOR = new Creator<SimpleNumberOption>() {
        @Override
        public SimpleNumberOption createFromParcel(Parcel in) {
            return new SimpleNumberOption(in);
        }

        @Override
        public SimpleNumberOption[] newArray(int size) {
            return new SimpleNumberOption[size];
        }
    };
    public Integer value;

    public SimpleNumberOption(int key, int title, boolean active) {
        super(SIMPLE_NUMBER, key, title, active);
    }

    public SimpleNumberOption(int key, int title, boolean active, int value) {
        super(SIMPLE_NUMBER, key, title, active);
        this.value = value;
    }

    protected SimpleNumberOption(Parcel in) {
        super(in);
        value = ParcelUtils.readObjectInteger(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelUtils.writeObjectInteger(dest, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SimpleNumberOption that = (SimpleNumberOption) o;
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
    public SimpleNumberOption clone() throws CloneNotSupportedException {
        SimpleNumberOption clone = (SimpleNumberOption) super.clone();
        clone.value = value;
        return clone;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
