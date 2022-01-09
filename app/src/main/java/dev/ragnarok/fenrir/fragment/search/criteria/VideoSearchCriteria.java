package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.search.options.SimpleBooleanOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleNumberOption;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;

public final class VideoSearchCriteria extends BaseSearchCriteria {

    public static final int KEY_SORT = 1;
    public static final int KEY_HD = 2;
    public static final int KEY_ADULT = 3;
    public static final int KEY_YOUTUBE = 4;
    public static final int KEY_VIMEO = 5;
    public static final int KEY_SHORT = 6;
    public static final int KEY_LONG = 7;
    public static final int KEY_SEARCH_OWN = 8;
    public static final int KEY_DURATION_FROM = 9;
    public static final int KEY_DURATION_TO = 10;
    public static final Creator<VideoSearchCriteria> CREATOR = new Creator<VideoSearchCriteria>() {
        @Override
        public VideoSearchCriteria createFromParcel(Parcel in) {
            return new VideoSearchCriteria(in);
        }

        @Override
        public VideoSearchCriteria[] newArray(int size) {
            return new VideoSearchCriteria[size];
        }
    };

    public VideoSearchCriteria(String query, boolean in_main_page) {
        super(query);

        SpinnerOption sort = new SpinnerOption(KEY_SORT, R.string.sorting, true);
        sort.available = new ArrayList<>(3);
        sort.available.add(new SpinnerOption.Entry(0, R.string.by_date_added));
        sort.available.add(new SpinnerOption.Entry(1, R.string.by_relevance));
        sort.available.add(new SpinnerOption.Entry(2, R.string.by_duration));
        appendOption(sort);

        appendOption(new SimpleBooleanOption(KEY_HD, R.string.hd_only, true));
        appendOption(new SimpleBooleanOption(KEY_ADULT, R.string.disable_safe_search, true, true));

        appendOption(new SimpleBooleanOption(KEY_YOUTUBE, R.string.youtube, true));
        appendOption(new SimpleBooleanOption(KEY_VIMEO, R.string.vimeo, true));
        appendOption(new SimpleBooleanOption(KEY_SHORT, R.string.short_videos, true));
        appendOption(new SimpleBooleanOption(KEY_LONG, R.string.long_videos, true));

        appendOption(new SimpleBooleanOption(KEY_SEARCH_OWN, R.string.search_in_my_videos, true, in_main_page));

        appendOption(new SimpleNumberOption(KEY_DURATION_FROM, R.string.min_duration, true));
        appendOption(new SimpleNumberOption(KEY_DURATION_TO, R.string.max_duration, true));
    }

    private VideoSearchCriteria(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public VideoSearchCriteria clone() throws CloneNotSupportedException {
        return (VideoSearchCriteria) super.clone();
    }
}
