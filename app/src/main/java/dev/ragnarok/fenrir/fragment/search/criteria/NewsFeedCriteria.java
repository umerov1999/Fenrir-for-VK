package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.search.options.SimpleDateOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleGPSOption;

public final class NewsFeedCriteria extends BaseSearchCriteria {

    public static final int KEY_GPS = 1;
    public static final int KEY_START_TIME = 2;
    public static final int KEY_END_TIME = 3;

    public static final Creator<NewsFeedCriteria> CREATOR = new Creator<NewsFeedCriteria>() {
        @Override
        public NewsFeedCriteria createFromParcel(Parcel in) {
            return new NewsFeedCriteria(in);
        }

        @Override
        public NewsFeedCriteria[] newArray(int size) {
            return new NewsFeedCriteria[size];
        }
    };

    public NewsFeedCriteria(String query) {
        super(query);

        appendOption(new SimpleGPSOption(KEY_GPS, R.string.gps, true));
        appendOption(new SimpleDateOption(KEY_START_TIME, R.string.date_start, true));
        appendOption(new SimpleDateOption(KEY_END_TIME, R.string.date_to, true));
    }

    private NewsFeedCriteria(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
