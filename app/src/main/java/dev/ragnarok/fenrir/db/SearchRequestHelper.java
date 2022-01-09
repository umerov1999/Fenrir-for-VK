package dev.ragnarok.fenrir.db;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import dev.ragnarok.fenrir.db.column.SearchRequestColumns;


public class SearchRequestHelper extends SQLiteOpenHelper {

    private static final Object lock = new Object();
    private static volatile SearchRequestHelper instance;

    private SearchRequestHelper(Context context) {
        super(context, "search_queries.sqlite", null, 1);
    }

    public static SearchRequestHelper getInstance(Context context) {
        if (isNull(instance)) {
            synchronized (lock) {
                if (isNull(instance)) {
                    instance = new SearchRequestHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSearchRequestTable(db);
    }

    private void createSearchRequestTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS [" + SearchRequestColumns.TABLENAME + "] (\n" +
                "  [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + SearchRequestColumns.SOURCE_ID + "] INTEGER, " +
                "  [" + SearchRequestColumns.QUERY + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
