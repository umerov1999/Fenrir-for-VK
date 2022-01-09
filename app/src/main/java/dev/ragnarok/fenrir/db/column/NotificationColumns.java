package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public class NotificationColumns implements BaseColumns {

    public static final String TABLENAME = "notifications";

    public static final String TYPE = "type";
    public static final String DATE = "date";
    public static final String DATA = "data";


    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_DATA = TABLENAME + "." + DATA;

    // This class cannot be instantiated
    private NotificationColumns() {
    }
}
