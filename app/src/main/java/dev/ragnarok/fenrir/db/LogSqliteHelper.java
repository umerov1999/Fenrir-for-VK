package dev.ragnarok.fenrir.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import dev.ragnarok.fenrir.db.column.LogColumns;
import dev.ragnarok.fenrir.util.Objects;

public class LogSqliteHelper extends SQLiteOpenHelper {

    private static final int V = 1;
    private static volatile LogSqliteHelper instance;

    private LogSqliteHelper(Context context) {
        super(context, "logs.sqlite", null, V);
    }

    public static LogSqliteHelper getInstance(Context context) {
        if (Objects.isNull(instance)) {
            synchronized (LogSqliteHelper.class) {
                if (Objects.isNull(instance)) {
                    instance = new LogSqliteHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS [" + LogColumns.TABLENAME + "] (\n" +
                "  [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + LogColumns.TYPE + "] INTEGER, " +
                "  [" + LogColumns.DATE + "] INTEGER, " +
                "  [" + LogColumns.TAG + "] TEXT, " +
                "  [" + LogColumns.BODY + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LogColumns.TABLENAME);
        onCreate(db);
    }

    public void Clear() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + LogColumns.TABLENAME);
        onCreate(db);
    }
}
