package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

import androidx.annotation.NonNull;

public final class DocumentSearchCriteria extends BaseSearchCriteria {

    public static final Creator<DocumentSearchCriteria> CREATOR = new Creator<DocumentSearchCriteria>() {
        @Override
        public DocumentSearchCriteria createFromParcel(Parcel in) {
            return new DocumentSearchCriteria(in);
        }

        @Override
        public DocumentSearchCriteria[] newArray(int size) {
            return new DocumentSearchCriteria[size];
        }
    };

    public DocumentSearchCriteria(String query) {
        super(query);
    }

    private DocumentSearchCriteria(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public DocumentSearchCriteria clone() throws CloneNotSupportedException {
        return (DocumentSearchCriteria) super.clone();
    }
}
