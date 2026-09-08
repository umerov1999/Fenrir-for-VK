package dev.ragnarok.fenrir.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.db.column.AudiosColumns
import dev.ragnarok.fenrir.db.column.FeedOwnersColumns
import dev.ragnarok.fenrir.db.column.LogsColumns
import dev.ragnarok.fenrir.db.column.ReactionsColumns
import dev.ragnarok.fenrir.db.column.SearchRequestColumns
import dev.ragnarok.fenrir.db.column.ShortcutsColumns
import dev.ragnarok.fenrir.db.column.StickerSetsColumns
import dev.ragnarok.fenrir.db.column.StickerSetsCustomColumns
import dev.ragnarok.fenrir.db.column.StickersKeywordsColumns
import dev.ragnarok.fenrir.db.column.TempDataColumns
import dev.ragnarok.fenrir.module.FenrirNative

class TempDataHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        if (!FenrirNative.isNativeLoaded) "temp_app_data.sqlite" else "temp_app_data_lz4.sqlite",
        null,
        Constants.DATABASE_TEMPORARY_VERSION
    ) {
    override fun onCreate(db: SQLiteDatabase) {
        createTmpDataTable(db)
        createSearchRequestTable(db)
        createLogsTable(db)
        createShortcutsColumn(db)
        createAudiosTable(db)
        createStickerSetsTable(db)
        createStickerSetsCustomTable(db)
        createStickersKeywordsTable(db)
        createReactionAssetsTable(db)
        createFeedOwnersTable(db)
    }

    private fun createReactionAssetsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + ReactionsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER NOT NULL, " +
                " [" + ReactionsColumns.ACCOUNT_ID + "] INTEGER, " +
                " [" + ReactionsColumns.STATIC + "] TEXT, " +
                " [" + ReactionsColumns.SMALL_ANIMATION + "] TEXT, " +
                " [" + ReactionsColumns.BIG_ANIMATION + "] TEXT, " +
                " UNIQUE ([" + BaseColumns._ID + "], [" + ReactionsColumns.ACCOUNT_ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createStickerSetsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + StickerSetsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER NOT NULL, " +
                " [" + StickerSetsColumns.ACCOUNT_ID + "] INTEGER, " +
                " [" + StickerSetsColumns.POSITION + "] INTEGER, " +
                " [" + StickerSetsColumns.TITLE + "] TEXT, " +
                " [" + StickerSetsColumns.PURCHASED + "] BOOLEAN, " +
                " [" + StickerSetsColumns.PROMOTED + "] BOOLEAN, " +
                " [" + StickerSetsColumns.ACTIVE + "] BOOLEAN, " +
                " [" + StickerSetsColumns.ICON + "] BLOB, " +
                " [" + StickerSetsColumns.STICKERS + "] BLOB, " +
                " UNIQUE ([" + BaseColumns._ID + "], [" + StickerSetsColumns.ACCOUNT_ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createStickerSetsCustomTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + StickerSetsCustomColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER NOT NULL, " +
                " [" + StickerSetsCustomColumns.ACCOUNT_ID + "] INTEGER, " +
                " [" + StickerSetsCustomColumns.POSITION + "] INTEGER, " +
                " [" + StickerSetsCustomColumns.TITLE + "] TEXT, " +
                " [" + StickerSetsCustomColumns.PURCHASED + "] BOOLEAN, " +
                " [" + StickerSetsCustomColumns.PROMOTED + "] BOOLEAN, " +
                " [" + StickerSetsCustomColumns.ACTIVE + "] BOOLEAN, " +
                " [" + StickerSetsCustomColumns.ICON + "] BLOB, " +
                " [" + StickerSetsCustomColumns.STICKERS + "] BLOB, " +
                " UNIQUE ([" + BaseColumns._ID + "], [" + StickerSetsCustomColumns.ACCOUNT_ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createStickersKeywordsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + StickersKeywordsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER NOT NULL, " +
                " [" + StickersKeywordsColumns.ACCOUNT_ID + "] INTEGER, " +
                " [" + StickersKeywordsColumns.KEYWORDS + "] BLOB, " +
                " [" + StickersKeywordsColumns.STICKERS + "] BLOB, " +
                " UNIQUE ([" + BaseColumns._ID + "], [" + StickersKeywordsColumns.ACCOUNT_ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createTmpDataTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + TempDataColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT REPLACE, " +
                " [" + TempDataColumns.OWNER_ID + "] INTEGER, " +
                " [" + TempDataColumns.SOURCE_ID + "] INTEGER, " +
                " [" + TempDataColumns.DATA + "] BLOB);"
        db.execSQL(sql)
    }

    private fun createSearchRequestTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + SearchRequestColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + SearchRequestColumns.SOURCE_ID + "] INTEGER, " +
                " [" + SearchRequestColumns.QUERY + "] TEXT, " +
                " UNIQUE ([" + SearchRequestColumns.SOURCE_ID + "], [" + SearchRequestColumns.QUERY + "]) ON CONFLICT IGNORE);"
        db.execSQL(sql)
    }

    private fun createShortcutsColumn(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + ShortcutsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT REPLACE, " +
                " [" + ShortcutsColumns.ACTION + "] TEXT, " +
                " [" + ShortcutsColumns.COVER + "] TEXT, " +
                " [" + ShortcutsColumns.NAME + "] TEXT);"
        db.execSQL(sql)
    }

    private fun createLogsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + LogsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT REPLACE, " +
                " [" + LogsColumns.TYPE + "] INTEGER, " +
                " [" + LogsColumns.DATE + "] INTEGER, " +
                " [" + LogsColumns.TAG + "] TEXT, " +
                " [" + LogsColumns.BODY + "] TEXT);"
        db.execSQL(sql)
    }

    private fun createAudiosTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + AudiosColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT REPLACE, " +
                " [" + AudiosColumns.SOURCE_OWNER_ID + "] INTEGER, " +
                " [" + AudiosColumns.AUDIO_ID + "] INTEGER, " +
                " [" + AudiosColumns.AUDIO_OWNER_ID + "] INTEGER, " +
                " [" + AudiosColumns.ARTIST + "] TEXT, " +
                " [" + AudiosColumns.TITLE + "] TEXT, " +
                " [" + AudiosColumns.DURATION + "] INTEGER, " +
                " [" + AudiosColumns.URL + "] TEXT, " +
                " [" + AudiosColumns.LYRICS_ID + "] INTEGER, " +
                " [" + AudiosColumns.DATE + "] INTEGER, " +
                " [" + AudiosColumns.ALBUM_ID + "] INTEGER, " +
                " [" + AudiosColumns.ALBUM_OWNER_ID + "] INTEGER, " +
                " [" + AudiosColumns.ALBUM_ACCESS_KEY + "] TEXT, " +
                " [" + AudiosColumns.GENRE + "] INTEGER, " +
                " [" + AudiosColumns.DELETED + "] BOOLEAN, " +
                " [" + AudiosColumns.ACCESS_KEY + "] TEXT, " +
                " [" + AudiosColumns.THUMB_IMAGE_BIG + "] TEXT, " +
                " [" + AudiosColumns.THUMB_IMAGE_VERY_BIG + "] TEXT, " +
                " [" + AudiosColumns.THUMB_IMAGE_LITTLE + "] TEXT, " +
                " [" + AudiosColumns.ALBUM_TITLE + "] TEXT, " +
                " [" + AudiosColumns.IS_HQ + "] BOOLEAN, " +
                " [" + AudiosColumns.MAIN_ARTISTS + "] BLOB);"
        db.execSQL(sql)
    }

    private fun createFeedOwnersTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + FeedOwnersColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT REPLACE, " +
                " [" + FeedOwnersColumns.TITLE + "] TEXT, " +
                " [" + FeedOwnersColumns.OWNERS_IDS + "] TEXT);"
        db.execSQL(sql)
    }

    fun clear() {
        val db = writableDatabase
        db.execSQL("DELETE FROM " + TempDataColumns.TABLENAME)
        db.execSQL("DELETE FROM " + SearchRequestColumns.TABLENAME)
        db.execSQL("DELETE FROM " + AudiosColumns.TABLENAME)
    }

    fun clearLogs() {
        val db = writableDatabase
        db.execSQL("DELETE FROM " + LogsColumns.TABLENAME)
    }

    private fun purge(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + TempDataColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + SearchRequestColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + LogsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + ShortcutsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + AudiosColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + StickerSetsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + StickerSetsCustomColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + StickersKeywordsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + ReactionsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + FeedOwnersColumns.TABLENAME)
        onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_TEMPORARY_VERSION) {
            purge(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_TEMPORARY_VERSION) {
            purge(db)
        }
    }

    companion object {
        val helper: TempDataHelper by lazy {
            TempDataHelper(Includes.provideApplicationContext())
        }
    }
}
