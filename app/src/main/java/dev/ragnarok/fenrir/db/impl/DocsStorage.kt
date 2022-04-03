package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getDocsContentUriFor
import dev.ragnarok.fenrir.db.column.DocColumns
import dev.ragnarok.fenrir.db.interfaces.IDocsStorage
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity.GraffitiDbo
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity.VideoPreviewDbo
import dev.ragnarok.fenrir.db.model.entity.PhotoSizeEntity
import dev.ragnarok.fenrir.model.DocFilter
import dev.ragnarok.fenrir.model.criteria.DocsCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Exestime.log
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class DocsStorage(base: AppStorages) : AbsStorage(base), IDocsStorage {
    override fun get(criteria: DocsCriteria): Single<List<DocumentEntity>> {
        return Single.create { e: SingleEmitter<List<DocumentEntity>> ->
            val start = System.currentTimeMillis()
            val uri = getDocsContentUriFor(criteria.accountId)
            val where: String
            val args: Array<String>
            val filter = criteria.filter
            if (filter != null && filter != DocFilter.Type.ALL) {
                where = DocColumns.OWNER_ID + " = ? AND " + DocColumns.TYPE + " = ?"
                args = arrayOf(criteria.ownerId.toString(), filter.toString())
            } else {
                where = DocColumns.OWNER_ID + " = ?"
                args = arrayOf(criteria.ownerId.toString())
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val data: MutableList<DocumentEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    data.add(map(cursor))
                }
                cursor.close()
            }
            e.onSuccess(data)
            log("DocsStorage.get", start, "count: " + data.size)
        }
    }

    override fun store(
        accountId: Int,
        ownerId: Int,
        entities: List<DocumentEntity>,
        clearBeforeInsert: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val start = System.currentTimeMillis()
            val uri = getDocsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeInsert) {
                operations.add(
                    ContentProviderOperation.newDelete(uri)
                        .withSelection(DocColumns.OWNER_ID + " = ?", arrayOf(ownerId.toString()))
                        .build()
                )
            }
            for (entity in entities) {
                val cv = ContentValues()
                cv.put(DocColumns.DOC_ID, entity.id)
                cv.put(DocColumns.OWNER_ID, entity.ownerId)
                cv.put(DocColumns.TITLE, entity.title)
                cv.put(DocColumns.SIZE, entity.size)
                cv.put(DocColumns.EXT, entity.ext)
                cv.put(DocColumns.URL, entity.url)
                cv.put(DocColumns.DATE, entity.date)
                cv.put(DocColumns.TYPE, entity.type)
                cv.put(DocColumns.ACCESS_KEY, entity.accessKey)
                cv.put(
                    DocColumns.PHOTO,
                    if (entity.photo != null) GSON.toJson(entity.photo) else null
                )
                cv.put(
                    DocColumns.GRAFFITI,
                    if (entity.graffiti != null) GSON.toJson(entity.graffiti) else null
                )
                cv.put(
                    DocColumns.VIDEO,
                    if (entity.video != null) GSON.toJson(entity.video) else null
                )
                operations.add(
                    ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
            log("DocsStorage.store", start, "count: " + entities.size)
        }
    }

    override fun delete(accountId: Int, docId: Int, ownerId: Int): Completable {
        return Completable.fromAction {
            val uri = getDocsContentUriFor(accountId)
            val where = DocColumns.DOC_ID + " = ? AND " + DocColumns.OWNER_ID + " = ?"
            val args = arrayOf(docId.toString(), ownerId.toString())
            contentResolver.delete(uri, where, args)
        }
    }

    companion object {
        private fun map(cursor: Cursor): DocumentEntity {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DocColumns.DOC_ID))
            val ownerId = cursor.getInt(cursor.getColumnIndexOrThrow(DocColumns.OWNER_ID))
            val document = DocumentEntity().set(id, ownerId)
                .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.TITLE)))
                .setSize(cursor.getLong(cursor.getColumnIndexOrThrow(DocColumns.SIZE)))
                .setExt(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.EXT)))
                .setUrl(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.URL)))
                .setType(cursor.getInt(cursor.getColumnIndexOrThrow(DocColumns.TYPE)))
                .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DocColumns.DATE)))
                .setAccessKey(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.ACCESS_KEY)))
            val photoJson = cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.PHOTO))
            val graffitiJson = cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.GRAFFITI))
            val videoJson = cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.VIDEO))
            if (photoJson.nonNullNoEmpty()) {
                document.photo = GSON.fromJson(photoJson, PhotoSizeEntity::class.java)
            }
            if (graffitiJson.nonNullNoEmpty()) {
                document.graffiti = GSON.fromJson(graffitiJson, GraffitiDbo::class.java)
            }
            if (videoJson.nonNullNoEmpty()) {
                document.video = GSON.fromJson(videoJson, VideoPreviewDbo::class.java)
            }
            return document
        }
    }
}