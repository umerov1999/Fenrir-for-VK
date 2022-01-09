package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class FaveArticlesColumns implements BaseColumns {

    public static final String TABLENAME = "fave_article";
    public static final String ARTICLE = "article";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_ARTICLE = TABLENAME + "." + ARTICLE;

    private FaveArticlesColumns() {
    }
}
