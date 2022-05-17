package dev.ragnarok.fenrir.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.db.column.LogColumns
import dev.ragnarok.fenrir.db.column.SearchRequestColumns
import dev.ragnarok.fenrir.db.column.ShortcutColumns
import dev.ragnarok.fenrir.db.column.TempDataColumns

class TempDataHelper(context: Context) :
    SQLiteOpenHelper(context, "temp_app_data.sqlite", null, Constants.DATABASE_TEMPORARY_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        createTmpDataTable(db)
        createSearchRequestTable(db)
        createLogsTable(db)
        createShortcutsColumn(db)
    }

    private fun createTmpDataTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + TempDataColumns.TABLENAME + "] (\n" +
                "  [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + TempDataColumns.OWNER_ID + "] INTEGER, " +
                "  [" + TempDataColumns.SOURCE_ID + "] INTEGER, " +
                "  [" + TempDataColumns.DATA + "] BLOB, " +
                "  CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createSearchRequestTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + SearchRequestColumns.TABLENAME + "] (\n" +
                "  [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + SearchRequestColumns.SOURCE_ID + "] INTEGER, " +
                "  [" + SearchRequestColumns.QUERY + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createShortcutsColumn(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + ShortcutColumns.TABLENAME + "] (\n" +
                "  [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + ShortcutColumns.ACTION + "] TEXT, " +
                "  [" + ShortcutColumns.COVER + "] TEXT, " +
                "  [" + ShortcutColumns.NAME + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createLogsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + LogColumns.TABLENAME + "] (\n" +
                "  [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + LogColumns.TYPE + "] INTEGER, " +
                "  [" + LogColumns.DATE + "] INTEGER, " +
                "  [" + LogColumns.TAG + "] TEXT, " +
                "  [" + LogColumns.BODY + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    fun clear() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + TempDataColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + SearchRequestColumns.TABLENAME)
        onCreate(db)
    }

    fun clearLogs() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + LogColumns.TABLENAME)
        onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_TEMPORARY_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TempDataColumns.TABLENAME)
            db.execSQL("DROP TABLE IF EXISTS " + SearchRequestColumns.TABLENAME)
            db.execSQL("DROP TABLE IF EXISTS " + LogColumns.TABLENAME)
            db.execSQL("DROP TABLE IF EXISTS " + ShortcutColumns.TABLENAME)
            onCreate(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_TEMPORARY_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TempDataColumns.TABLENAME)
            db.execSQL("DROP TABLE IF EXISTS " + SearchRequestColumns.TABLENAME)
            db.execSQL("DROP TABLE IF EXISTS " + LogColumns.TABLENAME)
            db.execSQL("DROP TABLE IF EXISTS " + ShortcutColumns.TABLENAME)
            onCreate(db)
        }
    }

    companion object {
        val helper: TempDataHelper by lazy {
            TempDataHelper(Includes.provideApplicationContext())
        }
    }
}