package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class FaveProductColumns implements BaseColumns {

    public static final String TABLENAME = "fave_product";
    public static final String PRODUCT = "product";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_PRODUCT = TABLENAME + "." + PRODUCT;

    private FaveProductColumns() {
    }
}
