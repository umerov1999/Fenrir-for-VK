package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;


public class SearchRequestColumns implements BaseColumns {

    public static final String TABLENAME = "search_app_request";

    public static final String SOURCE_ID = "source_id";
    public static final String QUERY = "query";

    // This class cannot be instantiated
    private SearchRequestColumns() {
    }
}