package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

public final class WallSearchCriteria extends BaseSearchCriteria {

    public static final Creator<WallSearchCriteria> CREATOR = new Creator<WallSearchCriteria>() {
        @Override
        public WallSearchCriteria createFromParcel(Parcel in) {
            return new WallSearchCriteria(in);
        }

        @Override
        public WallSearchCriteria[] newArray(int size) {
            return new WallSearchCriteria[size];
        }
    };
    private final int ownerId;

    public WallSearchCriteria(String query, int ownerId) {
        super(query);
        this.ownerId = ownerId;
    }

    private WallSearchCriteria(Parcel in) {
        super(in);
        ownerId = in.readInt();
    }

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(ownerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
