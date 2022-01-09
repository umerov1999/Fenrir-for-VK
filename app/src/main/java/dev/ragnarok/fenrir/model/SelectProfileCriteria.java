package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public final class SelectProfileCriteria implements Parcelable {

    public static final Creator<SelectProfileCriteria> CREATOR = new Creator<SelectProfileCriteria>() {
        @Override
        public SelectProfileCriteria createFromParcel(Parcel in) {
            return new SelectProfileCriteria(in);
        }

        @Override
        public SelectProfileCriteria[] newArray(int size) {
            return new SelectProfileCriteria[size];
        }
    };
    private @OwnerType
    int ownerType;

    public SelectProfileCriteria(Parcel in) {
        ownerType = in.readInt();
    }

    public SelectProfileCriteria() {
        ownerType = OwnerType.ALL_PEOPLE;
    }

    @OwnerType
    public int getOwnerType() {
        return ownerType;
    }

    public SelectProfileCriteria setOwnerType(@OwnerType int ownerType) {
        this.ownerType = ownerType;
        return this;
    }

    public boolean getIsPeopleOnly() {
        return ownerType == OwnerType.ALL_PEOPLE || ownerType == OwnerType.ONLY_FRIENDS;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ownerType);
    }

    @IntDef({OwnerType.ALL_PEOPLE,
            OwnerType.ONLY_FRIENDS,
            OwnerType.OWNERS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OwnerType {
        int ALL_PEOPLE = 1;
        int ONLY_FRIENDS = 2;
        int OWNERS = 3;
    }
}
