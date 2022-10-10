package dev.ragnarok.fenrir.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.db.column.StickersKeywordsColumns
import dev.ragnarok.fenrir.db.column.StikerSetColumns

class StickerDataHelper(context: Context) :
    SQLiteOpenHelper(context, "stickers.sqlite", null, Constants.DATABASE_STICKERS_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        createStickerSetTable(db)
        createStickersKeywordsTable(db)
    }

    private fun createStickerSetTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE [" + StikerSetColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                " [" + StikerSetColumns.ACCOUNT_ID + "] INTEGER, " +
                " [" + StikerSetColumns.POSITION + "] INTEGER, " +
                " [" + StikerSetColumns.TITLE + "] TEXT, " +
                " [" + StikerSetColumns.ICON + "] BLOB, " +
                " [" + StikerSetColumns.PURCHASED + "] BOOLEAN, " +
                " [" + StikerSetColumns.PROMOTED + "] BOOLEAN, " +
                " [" + StikerSetColumns.ACTIVE + "] BOOLEAN, " +
                " [" + StikerSetColumns.STICKERS + "] BLOB, " +
                " CONSTRAINT [] PRIMARY KEY([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createStickersKeywordsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE [" + StickersKeywordsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                " [" + StickersKeywordsColumns.ACCOUNT_ID + "] INTEGER, " +
                " [" + StickersKeywordsColumns.KEYWORDS + "] BLOB, " +
                " [" + StickersKeywordsColumns.STICKERS + "] BLOB, " +
                " CONSTRAINT [] PRIMARY KEY([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun purge(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + StikerSetColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + StickersKeywordsColumns.TABLENAME)
        onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_STICKERS_VERSION) {
            purge(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_STICKERS_VERSION) {
            purge(db)
        }
    }

    companion object {
        val helper: StickerDataHelper by lazy {
            StickerDataHelper(Includes.provideApplicationContext())
        }
    }
}
