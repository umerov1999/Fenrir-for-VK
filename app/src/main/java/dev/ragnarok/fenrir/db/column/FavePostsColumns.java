package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class FavePostsColumns implements BaseColumns {

    public static final String TABLENAME = "fave_posts";
    public static final String POST = "post";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_POST = TABLENAME + "." + POST;

    private FavePostsColumns() {
    }

}