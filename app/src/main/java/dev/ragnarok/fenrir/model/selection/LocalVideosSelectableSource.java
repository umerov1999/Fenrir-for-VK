package dev.ragnarok.fenrir.model.selection;

import android.os.Parcel;


public class LocalVideosSelectableSource extends AbsSelectableSource {

    public static final Creator<LocalVideosSelectableSource> CREATOR = new Creator<LocalVideosSelectableSource>() {
        @Override
        public LocalVideosSelectableSource createFromParcel(Parcel in) {
            return new LocalVideosSelectableSource(in);
        }

        @Override
        public LocalVideosSelectableSource[] newArray(int size) {
            return new LocalVideosSelectableSource[size];
        }
    };

    public LocalVideosSelectableSource() {
        super(Types.VIDEOS);
    }

    protected LocalVideosSelectableSource(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
