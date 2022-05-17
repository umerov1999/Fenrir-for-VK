package dev.ragnarok.fenrir.db.impl

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.db.column.StickersKeywordsColumns
import dev.ragnarok.fenrir.db.column.StikerSetColumns
import dev.ragnarok.fenrir.db.interfaces.IStickersStorage
import dev.ragnarok.fenrir.db.model.entity.StickerDboEntity
import dev.ragnarok.fenrir.db.model.entity.StickerSetEntity
import dev.ragnarok.fenrir.db.model.entity.StickersKeywordsEntity
import dev.ragnarok.fenrir.util.Exestime.log
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import dev.ragnarok.fenrir.util.msgpack.MsgPack
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
            val where = "${StikerSetColumns.PURCHASED} = ? AND ${StikerSetColumns.ACTIVE} = ?"
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

    override fun getKeywordsStickers(accountId: Int, s: String?): Single<List<StickerDboEntity>> {
        return Single.create { e: SingleEmitter<List<StickerDboEntity>> ->
            val cursor = helper(accountId).readableDatabase.query(
                StickersKeywordsColumns.TABLENAME,
                KEYWORDS_STICKER_COLUMNS,
                null,
                null,
                null,
                null,
                null
            )
            val stickers: MutableList<StickerDboEntity> = ArrayList(safeCountOf(cursor))
            while (cursor.moveToNext()) {
                if (e.isDisposed) {
                    break
                }
                val entity = mapStickersKeywords(cursor)
                for (v in entity.keywords.orEmpty()) {
                    if (s.equals(v, ignoreCase = true)) {
                        entity.stickers?.let { stickers.addAll(it) }
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
            StikerSetColumns.POSITION,
            StikerSetColumns.TITLE,
            StikerSetColumns.ICON,
            StikerSetColumns.PURCHASED,
            StikerSetColumns.PROMOTED,
            StikerSetColumns.ACTIVE,
            StikerSetColumns.STICKERS
        )
        private val KEYWORDS_STICKER_COLUMNS = arrayOf(
            BaseColumns._ID,
            StickersKeywordsColumns.KEYWORDS,
            StickersKeywordsColumns.STICKERS
        )
        private val COMPARATOR = Comparator { rhs: StickerSetEntity, lhs: StickerSetEntity ->
            lhs.position.compareTo(rhs.position)
        }

        private fun createCv(entity: StickerSetEntity, pos: Int): ContentValues {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, entity.id)
            cv.put(StikerSetColumns.POSITION, pos)
            entity.icon.ifNonNull({
                cv.put(
                    StikerSetColumns.ICON,
                    MsgPack.encodeToByteArray(ListSerializer(StickerSetEntity.Img.serializer()), it)
                )
            }, {
                cv.putNull(StikerSetColumns.ICON)
            })
            cv.put(StikerSetColumns.TITLE, entity.title)
            cv.put(StikerSetColumns.PURCHASED, entity.isPurchased)
            cv.put(StikerSetColumns.PROMOTED, entity.isPromoted)
            cv.put(StikerSetColumns.ACTIVE, entity.isActive)

            entity.stickers.ifNonNull({
                cv.put(
                    StikerSetColumns.STICKERS,
                    MsgPack.encodeToByteArray(ListSerializer(StickerDboEntity.serializer()), it)
                )
            }, {
                cv.putNull(StikerSetColumns.STICKERS)
            })
            return cv
        }

        private fun createCvStickersKeywords(
            entity: StickersKeywordsEntity,
            id: Int
        ): ContentValues {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, id)
            entity.keywords.ifNonNull({
                cv.put(
                    StickersKeywordsColumns.KEYWORDS,
                    MsgPack.encodeToByteArray(ListSerializer(String.serializer()), it)
                )
            }, {
                cv.putNull(StickersKeywordsColumns.KEYWORDS)
            })
            entity.stickers.ifNonNull({
                cv.put(
                    StickersKeywordsColumns.STICKERS,
                    MsgPack.encodeToByteArray(ListSerializer(StickerDboEntity.serializer()), it)
                )
            }, {
                cv.putNull(StickersKeywordsColumns.STICKERS)
            })
            return cv
        }

        private fun map(cursor: Cursor): StickerSetEntity {
            val stickersJson = cursor.getBlob(StikerSetColumns.STICKERS)
            val iconJson = cursor.getBlob(StikerSetColumns.ICON)
            return StickerSetEntity(cursor.getInt(BaseColumns._ID))
                .setStickers(
                    if (stickersJson == null) null else MsgPack.decodeFromByteArray(
                        ListSerializer(StickerDboEntity.serializer()),
                        stickersJson
                    )
                )
                .setActive(cursor.getBoolean(StikerSetColumns.ACTIVE))
                .setPurchased(cursor.getBoolean(StikerSetColumns.PURCHASED))
                .setPromoted(cursor.getBoolean(StikerSetColumns.PROMOTED))
                .setIcon(
                    if (iconJson == null) null else MsgPack.decodeFromByteArray(
                        ListSerializer(
                            StickerSetEntity.Img.serializer()
                        ), iconJson
                    )
                )
                .setPosition(cursor.getInt(StikerSetColumns.POSITION))
                .setTitle(cursor.getString(StikerSetColumns.TITLE))
        }

        private fun mapStickersKeywords(cursor: Cursor): StickersKeywordsEntity {
            val stickersJson =
                cursor.getBlob(StickersKeywordsColumns.STICKERS)
            val keywordsJson =
                cursor.getBlob(StickersKeywordsColumns.KEYWORDS)
            return StickersKeywordsEntity(
                if (keywordsJson == null) null else MsgPack.decodeFromByteArray(
                    ListSerializer(
                        String.serializer()
                    ), keywordsJson
                ),
                if (stickersJson == null) null else MsgPack.decodeFromByteArray(
                    ListSerializer(
                        StickerDboEntity.serializer()
                    ), stickersJson
                ),
            )
        }
    }
}