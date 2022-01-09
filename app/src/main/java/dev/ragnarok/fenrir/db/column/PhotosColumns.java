package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class PhotosColumns implements BaseColumns {

    public static final String TABLENAME = "photos";
    //Columns
    public static final String PHOTO_ID = "photo_id";
    public static final String ALBUM_ID = "album_id";
    public static final String OWNER_ID = "owner_id";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String TEXT = "text";
    public static final String DATE = "date";
    public static final String SIZES = "sizes";
    public static final String USER_LIKES = "user_likes";
    public static final String CAN_COMMENT = "can_comment";
    public static final String LIKES = "likes";
    public static final String COMMENTS = "comments";
    public static final String TAGS = "tags";
    public static final String ACCESS_KEY = "access_key";
    public static final String DELETED = "deleted";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_PHOTO_ID = TABLENAME + "." + PHOTO_ID;
    public static final String FULL_ALBUM_ID = TABLENAME + "." + ALBUM_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_WIDTH = TABLENAME + "." + WIDTH;
    public static final String FULL_HEIGHT = TABLENAME + "." + HEIGHT;
    public static final String FULL_TEXT = TABLENAME + "." + TEXT;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_SIZES = TABLENAME + "." + SIZES;
    public static final String FULL_USER_LIKES = TABLENAME + "." + USER_LIKES;
    public static final String FULL_CAN_COMMENT = TABLENAME + "." + CAN_COMMENT;
    public static final String FULL_LIKES = TABLENAME + "." + LIKES;
    public static final String FULL_COMMENTS = TABLENAME + "." + COMMENTS;
    public static final String FULL_TAGS = TABLENAME + "." + TAGS;
    public static final String FULL_ACCESS_KEY = TABLENAME + "." + ACCESS_KEY;
    public static final String FULL_DELETED = TABLENAME + "." + DELETED;

    private PhotosColumns() {
    }
}