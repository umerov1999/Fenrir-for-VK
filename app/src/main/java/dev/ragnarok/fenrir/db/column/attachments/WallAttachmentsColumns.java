package dev.ragnarok.fenrir.db.column.attachments;

import android.provider.BaseColumns;

public final class WallAttachmentsColumns implements BaseColumns {

    public static final String TABLENAME = "wall_attachments";
    public static final String P_ID = "post_id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_P_ID = TABLENAME + "." + P_ID;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_DATA = TABLENAME + "." + DATA;

    private WallAttachmentsColumns() {
    }
}