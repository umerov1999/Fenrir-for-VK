package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;


public class LogColumns implements BaseColumns {

    public static final String TABLENAME = "logs";
    public static final String TYPE = "eventtype";
    public static final String DATE = "eventdate";
    public static final String TAG = "tag";
    public static final String BODY = "body";

    // This class cannot be instantiated
    private LogColumns() {
    }
}
