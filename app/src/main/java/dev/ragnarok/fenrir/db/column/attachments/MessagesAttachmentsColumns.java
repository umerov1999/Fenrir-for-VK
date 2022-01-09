package dev.ragnarok.fenrir.db.column.attachments;

import android.provider.BaseColumns;

public final class MessagesAttachmentsColumns implements BaseColumns {

    public static final String TABLENAME = "messages_attachments";
    public static final String M_ID = "message_id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_M_ID = TABLENAME + "." + M_ID;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_DATA = TABLENAME + "." + DATA;

    private MessagesAttachmentsColumns() {
    }
}