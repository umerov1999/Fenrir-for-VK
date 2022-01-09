package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class DocColumns implements BaseColumns {

    public static final String TABLENAME = "docs";
    public static final String DOC_ID = "doc_id";
    public static final String OWNER_ID = "owner_id";
    public static final String TITLE = "title";
    public static final String SIZE = "size";
    public static final String EXT = "ext";
    public static final String URL = "url";
    public static final String PHOTO = "photo";
    public static final String GRAFFITI = "graffiti";
    public static final String VIDEO = "video";
    public static final String DATE = "date";
    public static final String TYPE = "type";
    public static final String ACCESS_KEY = "access_key";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_DOC_ID = TABLENAME + "." + DOC_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_SIZE = TABLENAME + "." + SIZE;
    public static final String FULL_EXT = TABLENAME + "." + EXT;
    public static final String FULL_URL = TABLENAME + "." + URL;
    public static final String FULL_PHOTO = TABLENAME + "." + PHOTO;
    public static final String FULL_GRAFFITI = TABLENAME + "." + GRAFFITI;
    public static final String FULL_VIDEO = TABLENAME + "." + VIDEO;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_ACCESS_KEY = TABLENAME + "." + ACCESS_KEY;

    private DocColumns() {
    }
}