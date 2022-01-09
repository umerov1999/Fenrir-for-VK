package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;


public class TempDataColumns implements BaseColumns {

    public static final String TABLENAME = "temp_app_data";

    public static final String OWNER_ID = "owner_id";
    public static final String SOURCE_ID = "source_id";
    public static final String DATA = "data";

    // This class cannot be instantiated
    private TempDataColumns() {
    }
}