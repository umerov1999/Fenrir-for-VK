package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public class KeyColumns implements BaseColumns {

    public static final String TABLENAME = "keys";
    public static final String VERSION = "version";
    public static final String PEER_ID = "peer_id";
    public static final String SESSION_ID = "session_id";
    public static final String DATE = "date";
    public static final String START_SESSION_MESSAGE_ID = "start_mid";
    public static final String END_SESSION_MESSAGE_ID = "end_mid";
    public static final String OUT_KEY = "outkey";
    public static final String IN_KEY = "inkey";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_VERSION = TABLENAME + "." + VERSION;
    public static final String FULL_PEER_ID = TABLENAME + "." + PEER_ID;
    public static final String FULL_SESSION_ID = TABLENAME + "." + SESSION_ID;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_START_SESSION_MESSAGE_ID = TABLENAME + "." + START_SESSION_MESSAGE_ID;
    public static final String FULL_END_SESSION_MESSAGE_ID = TABLENAME + "." + END_SESSION_MESSAGE_ID;
    public static final String FULL_OUT_KEY = TABLENAME + "." + OUT_KEY;
    public static final String FULL_IN_KEY = TABLENAME + "." + IN_KEY;

    private KeyColumns() {
    }
}
