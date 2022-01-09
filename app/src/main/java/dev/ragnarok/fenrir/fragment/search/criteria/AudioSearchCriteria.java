package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.search.options.SimpleBooleanOption;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;

public class AudioSearchCriteria extends BaseSearchCriteria {
    public static final int KEY_SEARCH_ADDED = 1;
    public static final int KEY_SEARCH_BY_ARTIST = 2;
    public static final int KEY_SEARCH_AUTOCOMPLETE = 3;
    public static final int KEY_SEARCH_WITH_LYRICS = 4;
    public static final int KEY_SORT = 5;
    public static final Creator<AudioSearchCriteria> CREATOR = new Creator<AudioSearchCriteria>() {
        @Override
        public AudioSearchCriteria createFromParcel(Parcel in) {
            return new AudioSearchCriteria(in);
        }

        @Override
        public AudioSearchCriteria[] newArray(int size) {
            return new AudioSearchCriteria[size];
        }
    };

    public AudioSearchCriteria(String query, boolean by_artist, boolean in_main_page) {
        super(query);
        SpinnerOption sort = new SpinnerOption(KEY_SORT, R.string.sorting, true);
        sort.available = new ArrayList<>(3);
        sort.available.add(new SpinnerOption.Entry(0, R.string.by_date_added));
        sort.available.add(new SpinnerOption.Entry(1, R.string.by_relevance));
        sort.available.add(new SpinnerOption.Entry(2, R.string.by_duration));
        appendOption(sort);

        appendOption(new SimpleBooleanOption(KEY_SEARCH_ADDED, R.string.my_saved, true, in_main_page));
        appendOption(new SimpleBooleanOption(KEY_SEARCH_BY_ARTIST, R.string.by_artist, true, by_artist));
        appendOption(new SimpleBooleanOption(KEY_SEARCH_AUTOCOMPLETE, R.string.auto_compete, true));
        appendOption(new SimpleBooleanOption(KEY_SEARCH_WITH_LYRICS, R.string.with_lyrics, true));
    }

    protected AudioSearchCriteria(Parcel in) {
        super(in);
    }

    @NonNull
    @Override
    public AudioSearchCriteria clone() throws CloneNotSupportedException {
        return (AudioSearchCriteria) super.clone();
    }
}
