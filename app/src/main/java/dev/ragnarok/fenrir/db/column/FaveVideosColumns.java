package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class FaveVideosColumns implements BaseColumns {

    public static final String TABLENAME = "fave_videos";
    public static final String VIDEO = "video";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_VIDEO = TABLENAME + "." + VIDEO;

    private FaveVideosColumns() {
    }

}