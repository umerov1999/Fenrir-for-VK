package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

public class ArtistSearchCriteria extends BaseSearchCriteria {

    public static final Creator<ArtistSearchCriteria> CREATOR = new Creator<ArtistSearchCriteria>() {
        @Override
        public ArtistSearchCriteria createFromParcel(Parcel in) {
            return new ArtistSearchCriteria(in);
        }

        @Override
        public ArtistSearchCriteria[] newArray(int size) {
            return new ArtistSearchCriteria[size];
        }
    };

    public ArtistSearchCriteria(String query) {
        super(query);
    }

    private ArtistSearchCriteria(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
