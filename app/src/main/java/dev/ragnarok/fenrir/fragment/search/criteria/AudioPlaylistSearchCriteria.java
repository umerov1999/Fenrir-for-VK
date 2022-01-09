package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

public final class AudioPlaylistSearchCriteria extends BaseSearchCriteria {

    public static final Creator<AudioPlaylistSearchCriteria> CREATOR = new Creator<AudioPlaylistSearchCriteria>() {
        @Override
        public AudioPlaylistSearchCriteria createFromParcel(Parcel in) {
            return new AudioPlaylistSearchCriteria(in);
        }

        @Override
        public AudioPlaylistSearchCriteria[] newArray(int size) {
            return new AudioPlaylistSearchCriteria[size];
        }
    };

    public AudioPlaylistSearchCriteria(String query) {
        super(query);
    }

    private AudioPlaylistSearchCriteria(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
