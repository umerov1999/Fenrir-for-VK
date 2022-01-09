package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public class StikerSetColumns implements BaseColumns {

    public static final String TABLENAME = "sticker_set";
    public static final String POSITION = "position";
    public static final String TITLE = "title";
    public static final String ICON = "icon";
    public static final String PURCHASED = "purchased";
    public static final String PROMOTED = "promoted";
    public static final String ACTIVE = "active";
    public static final String STICKERS = "stickers";

    private StikerSetColumns() {
    }
}
