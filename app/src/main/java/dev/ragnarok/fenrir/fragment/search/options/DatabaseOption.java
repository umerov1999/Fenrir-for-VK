package dev.ragnarok.fenrir.fragment.search.options;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class DatabaseOption extends BaseOption {

    public static final int TYPE_COUNTRY = 1;
    public static final int TYPE_CITY = 2;
    public static final int TYPE_UNIVERSITY = 3;
    public static final int TYPE_FACULTY = 4;
    public static final int TYPE_CHAIR = 5;
    public static final int TYPE_SCHOOL = 6;
    public static final int TYPE_SCHOOL_CLASS = 7;
    public static final Creator<DatabaseOption> CREATOR = new Creator<DatabaseOption>() {
        @Override
        public DatabaseOption createFromParcel(Parcel in) {
            return new DatabaseOption(in);
        }

        @Override
        public DatabaseOption[] newArray(int size) {
            return new DatabaseOption[size];
        }
    };
    /**
     * Тип данных, который находится в обьекте value
     * страна, город, университет и т.д.
     */
    public final int type;
    /**
     * Текущее значение опции
     */
    public Entry value;

    public DatabaseOption(int key, int title, boolean active, int type) {
        super(DATABASE, key, title, active);
        this.type = type;
    }

    protected DatabaseOption(Parcel in) {
        super(in);
        type = in.readInt();
        value = in.readParcelable(Entry.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(type);
        dest.writeParcelable(value, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DatabaseOption that = (DatabaseOption) o;
        return type == that.type
                && (Objects.equals(value, that.value));
    }

    @NonNull
    @Override
    public DatabaseOption clone() throws CloneNotSupportedException {
        DatabaseOption clone = (DatabaseOption) super.clone();
        clone.value = value == null ? null : value.clone();
        return clone;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public void reset() {
        value = null;
    }

    public static class Entry implements Parcelable, Cloneable {

        public static final Creator<Entry> CREATOR = new Creator<Entry>() {
            @Override
            public Entry createFromParcel(Parcel in) {
                return new Entry(in);
            }

            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };
        public final int id;
        public final String title;

        public Entry(int id, String title) {
            this.id = id;
            this.title = title;
        }

        protected Entry(Parcel in) {
            id = in.readInt();
            title = in.readString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;
            return id == entry.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(title);
        }

        @NonNull
        @Override
        protected Entry clone() throws CloneNotSupportedException {
            return (Entry) super.clone();
        }
    }
}
