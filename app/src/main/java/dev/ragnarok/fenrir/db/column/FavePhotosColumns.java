package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class FavePhotosColumns implements BaseColumns {

    public static final String TABLENAME = "fave_photos";
    public static final String PHOTO_ID = "photo_id";
    public static final String OWNER_ID = "owner_id";
    public static final String POST_ID = "post_id";
    public static final String PHOTO = "photo";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_PHOTO_ID = TABLENAME + "." + PHOTO_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_POST_ID = TABLENAME + "." + POST_ID;
    public static final String FULL_PHOTO = TABLENAME + "." + PHOTO;

    private FavePhotosColumns() {
    }

}