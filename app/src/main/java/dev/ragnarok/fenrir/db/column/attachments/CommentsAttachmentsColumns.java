package dev.ragnarok.fenrir.db.column.attachments;

import android.provider.BaseColumns;

public final class CommentsAttachmentsColumns implements BaseColumns {

    public static final String TABLENAME = "comments_attachments";
    public static final String C_ID = "comment_id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_C_ID = TABLENAME + "." + C_ID;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_DATA = TABLENAME + "." + DATA;

    private CommentsAttachmentsColumns() {
    }
}