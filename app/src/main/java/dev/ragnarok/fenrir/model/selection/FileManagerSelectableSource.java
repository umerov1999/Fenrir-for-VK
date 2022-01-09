package dev.ragnarok.fenrir.model.selection;

import android.os.Parcel;


public class FileManagerSelectableSource extends AbsSelectableSource {

    public static final Creator<FileManagerSelectableSource> CREATOR = new Creator<FileManagerSelectableSource>() {
        @Override
        public FileManagerSelectableSource createFromParcel(Parcel in) {
            return new FileManagerSelectableSource(in);
        }

        @Override
        public FileManagerSelectableSource[] newArray(int size) {
            return new FileManagerSelectableSource[size];
        }
    };

    public FileManagerSelectableSource() {
        super(Types.FILES);
    }

    protected FileManagerSelectableSource(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}