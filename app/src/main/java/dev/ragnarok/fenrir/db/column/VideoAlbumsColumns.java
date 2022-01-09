package dev.ragnarok.fenrir.db.column;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import android.content.ContentValues;
import android.provider.BaseColumns;

import dev.ragnarok.fenrir.api.model.VKApiVideoAlbum;

public final class VideoAlbumsColumns implements BaseColumns {

    public static final String TABLENAME = "video_albums";

    public static final String ALBUM_ID = "album_id";
    public static final String OWNER_ID = "owner_id";
    public static final String TITLE = "title";
    public static final String IMAGE = "image";
    public static final String COUNT = "count";
    public static final String UPDATE_TIME = "update_time";
    public static final String PRIVACY = "privacy";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_ALBUM_ID = TABLENAME + "." + ALBUM_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_IMAGE = TABLENAME + "." + IMAGE;
    public static final String FULL_COUNT = TABLENAME + "." + COUNT;
    public static final String FULL_UPDATE_TIME = TABLENAME + "." + UPDATE_TIME;
    public static final String FULL_PRIVACY = TABLENAME + "." + PRIVACY;

    // This class cannot be instantiated
    private VideoAlbumsColumns() {
    }

    public static ContentValues getCV(VKApiVideoAlbum p) {
        ContentValues cv = new ContentValues();
        cv.put(OWNER_ID, p.owner_id);
        cv.put(ALBUM_ID, p.id);
        cv.put(TITLE, p.title);
        cv.put(IMAGE, p.image);
        cv.put(COUNT, p.count);
        cv.put(UPDATE_TIME, p.updated_time);
        cv.put(PRIVACY, isNull(p.privacy) ? null : p.privacy.toString());
        return cv;
    }
}
