package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

public class IdPair implements Parcelable {

    public static final Creator<IdPair> CREATOR = new Creator<IdPair>() {
        @Override
        public IdPair createFromParcel(Parcel in) {
            return new IdPair(in);
        }

        @Override
        public IdPair[] newArray(int size) {
            return new IdPair[size];
        }
    };
    public final int id;
    public final int ownerId;

    public IdPair(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected IdPair(Parcel in) {
        id = in.readInt();
        ownerId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(ownerId);
    }

    public boolean isValid() {
        return id != 0 && ownerId != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdPair idPair = (IdPair) o;
        if (id != idPair.id) return false;
        return ownerId == idPair.ownerId;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + ownerId;
        return result;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getId() {
        return id;
    }
}
