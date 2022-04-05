package dev.ragnarok.fenrir.db.impl

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import com.google.gson.reflect.TypeToken
import dev.ragnarok.fenrir.db.column.StickersKeywordsColumns
import dev.ragnarok.fenrir.db.column.StickersKeywordsColumns.STICKERS
import dev.ragnarok.fenrir.db.column.StikerSetColumns
import dev.ragnarok.fenrir.db.column.StikerSetColumns.ACTIVE
import dev.ragnarok.fenrir.db.column.StikerSetColumns.ICON
import dev.ragnarok.fenrir.db.column.StikerSetColumns.POSITION
import dev.ragnarok.fenrir.db.column.StikerSetColumns.PROMOTED
import dev.ragnarok.fenrir.db.column.StikerSetColumns.PURCHASED
import dev.ragnarok.fenrir.db.column.StikerSetColumns.TITLE
import dev.ragnarok.fenrir.db.interfaces.IStickersStorage
import dev.ragnarok.fenrir.db.model.entity.StickerEntity
import dev.ragnarok.fenrir.db.model.entity.StickerSetEntity
import dev.ragnarok.fenrir.db.model.entity.StickersKeywordsEntity
import dev.ragnarok.fenrir.util.Exestime.log
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.util.*

internal class StickersStorage(base: AppStorages) : AbsStorage(base), IStickersStorage {
    override fun store(accountId: Int, sets: List<StickerSetEntity>): Completable {
        return Completable.create { e: CompletableEmitter ->
            val start = System.currentTimeMillis()
            val db = helper(accountId).writableDatabase
            db.beginTransaction()
            try {
                db.delete(StikerSetColumns.TABLENAME, null, null)
                for ((i, entity) in sets.withIndex()) {
                    db.insert(StikerSetColumns.TABLENAME, null, createCv(entity, i))
                }
                db.setTransactionSuccessful()
                db.endTransaction()
                e.onComplete()
            } catch (exception: Exception) {
                db.endTransaction()
                e.tryOnError(exception)
            }
            log("StickersStorage.store", start, "count: " + safeCountOf(sets))
        }
    }

    override fun storeKeyWords(accountId: Int, sets: List<StickersKeywordsEntity>): Completable {
        return Completable.create { e: CompletableEmitter ->
            val start = System.currentTimeMillis()
            val db = helper(accountId).writableDatabase
            db.beginTransaction()
            try {
                db.delete(StickersKeywordsColumns.TABLENAME, null, null)
                for ((id, entity) in sets.withIndex()) {
                    db.insert(
                        StickersKeywordsColumns.TABLENAME,
                        null,
                        createCvStickersKeywords(entity, id)
                    )
                }
                db.setTransactionSuccessful()
                db.endTransaction()
                e.onComplete()
            } catch (exception: Exception) {
                db.endTransaction()
                e.tryOnError(exception)
            }
            log("StickersStorage.storeKeyWords", start, "count: " + safeCountOf(sets))
        }
    }

    override fun getPurchasedAndActive(accountId: Int): Single<List<StickerSetEntity>> {
        return Single.create { e: SingleEmitter<List<StickerSetEntity>> ->
            val start = System.currentTimeMillis()
            val where = "$PURCHASED = ? AND $ACTIVE = ?"
            val args = arrayOf("1", "1")
            val cursor = helper(accountId).readableDatabase.query(
                StikerSetColumns.TABLENAME,
                COLUMNS,
                where,
                args,
                null,
                null,
                null
            )
            val stickers: MutableList<StickerSetEntity> = ArrayList(cursor.count)
            while (cursor.moveToNext()) {
                if (e.isDisposed) {
                    break
                }
                stickers.add(map(cursor))
            }
            Collections.sort(stickers, COMPARATOR)
            cursor.close()
            e.onSuccess(stickers)
            log("StickersStorage.get", start, "count: " + stickers.size)
        }
    }

    override fun getKeywordsStickers(accountId: Int, s: String?): Single<List<StickerEntity>> {
        return Single.create { e: SingleEmitter<List<StickerEntity>> ->
            val cursor = helper(accountId).readableDatabase.query(
                StickersKeywordsColumns.TABLENAME,
                KEYWORDS_STICKER_COLUMNS,
                null,
                null,
                null,
                null,
                null
            )
            val stickers: MutableList<StickerEntity> = ArrayList(safeCountOf(cursor))
            while (cursor.moveToNext()) {
                if (e.isDisposed) {
                    break
                }
                val entity = mapStickersKeywords(cursor)
                for (v in entity.keywords) {
                    if (s.equals(v, ignoreCase = true)) {
                        stickers.addAll(entity.stickers)
                        cursor.close()
                        e.onSuccess(stickers)
                        return@create
                    }
                }
            }
            cursor.close()
            e.onSuccess(stickers)
        }
    }

    companion object {
        private val COLUMNS = arrayOf(
            BaseColumns._ID,
            POSITION,
            TITLE,
            ICON,
            PURCHASED,
            PROMOTED,
            ACTIVE,
            STICKERS
        )
        private val KEYWORDS_STICKER_COLUMNS = arrayOf(
            BaseColumns._ID,
            StickersKeywordsColumns.KEYWORDS,
            STICKERS
        )
        private val TYPE = object : TypeToken<List<StickerEntity?>?>() {}.type
        private val WORDS = object : TypeToken<List<String?>?>() {}.type
        private val ICONS = object : TypeToken<List<StickerSetEntity.Img?>?>() {}.type
        private val COMPARATOR = Comparator { rhs: StickerSetEntity, lhs: StickerSetEntity ->
            lhs.position.compareTo(rhs.position)
        }

        private fun createCv(entity: StickerSetEntity, pos: Int): ContentValues {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, entity.id)
            cv.put(POSITION, pos)
            cv.put(ICON, GSON.toJson(entity.icon))
            cv.put(TITLE, entity.title)
            cv.put(PURCHASED, entity.isPurchased)
            cv.put(PROMOTED, entity.isPromoted)
            cv.put(ACTIVE, entity.isActive)
            cv.put(STICKERS, GSON.toJson(entity.stickers))
            return cv
        }

        private fun createCvStickersKeywords(
            entity: StickersKeywordsEntity,
            id: Int
        ): ContentValues {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, id)
            cv.put(StickersKeywordsColumns.KEYWORDS, GSON.toJson(entity.keywords))
            cv.put(STICKERS, GSON.toJson(entity.stickers))
            return cv
        }

        private fun map(cursor: Cursor): StickerSetEntity {
            val stickersJson = cursor.getString(cursor.getColumnIndexOrThrow(STICKERS))
            val iconJson = cursor.getString(cursor.getColumnIndexOrThrow(ICON))
            return StickerSetEntity(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                .setStickers(GSON.fromJson(stickersJson, TYPE))
                .setActive(cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVE)) == 1)
                .setPurchased(cursor.getInt(cursor.getColumnIndexOrThrow(PURCHASED)) == 1)
                .setPromoted(cursor.getInt(cursor.getColumnIndexOrThrow(PROMOTED)) == 1)
                .setIcon(GSON.fromJson(iconJson, ICONS))
                .setPosition(cursor.getInt(cursor.getColumnIndexOrThrow(POSITION)))
                .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE)))
        }

        private fun mapStickersKeywords(cursor: Cursor): StickersKeywordsEntity {
            val stickersJson =
                cursor.getString(cursor.getColumnIndexOrThrow(STICKERS))
            val keywordsJson =
                cursor.getString(cursor.getColumnIndexOrThrow(StickersKeywordsColumns.KEYWORDS))
            return StickersKeywordsEntity(
                GSON.fromJson(keywordsJson, WORDS),
                GSON.fromJson(stickersJson, TYPE)
            )
        }
    }
}