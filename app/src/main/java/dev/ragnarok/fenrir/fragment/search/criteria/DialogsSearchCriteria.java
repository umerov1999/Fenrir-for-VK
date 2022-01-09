package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

public class DialogsSearchCriteria extends BaseSearchCriteria {

    public static final Creator<DialogsSearchCriteria> CREATOR = new Creator<DialogsSearchCriteria>() {
        @Override
        public DialogsSearchCriteria createFromParcel(Parcel in) {
            return new DialogsSearchCriteria(in);
        }

        @Override
        public DialogsSearchCriteria[] newArray(int size) {
            return new DialogsSearchCriteria[size];
        }
    };

    public DialogsSearchCriteria(String query) {
        super(query);
    }

    private DialogsSearchCriteria(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
