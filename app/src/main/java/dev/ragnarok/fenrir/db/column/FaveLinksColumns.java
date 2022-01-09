package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class FaveLinksColumns implements BaseColumns {

    public static final String TABLENAME = "fave_link";
    public static final String LINK_ID = "link_id";
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String PHOTO = "photo";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_LINK_ID = TABLENAME + "." + LINK_ID;
    public static final String FULL_URL = TABLENAME + "." + URL;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_DESCRIPTION = TABLENAME + "." + DESCRIPTION;
    public static final String FULL_PHOTO = TABLENAME + "." + PHOTO;

    private FaveLinksColumns() {
    }
}