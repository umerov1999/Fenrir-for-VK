package dev.ragnarok.fenrir.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.api.model.VKApiCountry;

public class CountriesColumns implements BaseColumns {

    public static final String TABLENAME = "countries";

    public static final String NAME = "name";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_NAME = TABLENAME + "." + NAME;

    // This class cannot be instantiated
    private CountriesColumns() {
    }

    public static ContentValues getCV(@NonNull VKApiCountry country) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, country.id);
        cv.put(NAME, country.title);
        return cv;
    }
}
