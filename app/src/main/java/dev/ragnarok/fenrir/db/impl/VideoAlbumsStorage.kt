package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getVideoAlbumsContentUriFor
import dev.ragnarok.fenrir.db.column.VideoAlbumsColumns
import dev.ragnarok.fenrir.db.interfaces.IVideoAlbumsStorage
import dev.ragnarok.fenrir.db.model.entity.PrivacyEntity
import dev.ragnarok.fenrir.db.model.entity.VideoAlbumEntity
import dev.ragnarok.fenrir.model.VideoAlbumCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class VideoAlbumsStorage(base: AppStorages) : AbsStorage(base), IVideoAlbumsStorage {
    override fun findByCriteria(criteria: VideoAlbumCriteria): Single<List<VideoAlbumEntity>> {
        return Single.create { e: SingleEmitter<List<VideoAlbumEntity>> ->
            val uri = getVideoAlbumsContentUriFor(criteria.accountId)
            val where: String
            val args: Array<String>
            val range = criteria.range
            if (range != null) {
                where = VideoAlbumsColumns.OWNER_ID + " = ? " +
                        " AND " + BaseColumns._ID + " >= ? " +
                        " AND " + BaseColumns._ID + " <= ?"
                args = arrayOf(
                    criteria.ownerId.toString(),
                    range.first.toString(),
                    range.last.toString()
                )
            } else {
                where = VideoAlbumsColumns.OWNER_ID + " = ?"
                args = arrayOf(criteria.ownerId.toString())
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val data: MutableList<VideoAlbumEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    data.add(mapAlbum(cursor))
                }
                cursor.close()
            }
            e.onSuccess(data)
        }
    }

    override fun insertData(
        accountId: Int,
        ownerId: Int,
        data: List<VideoAlbumEntity>,
        invalidateBefore: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val uri = getVideoAlbumsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (invalidateBefore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .withSelection(
                            VideoAlbumsColumns.OWNER_ID + " = ?",
                            arrayOf(ownerId.toString())
                        )
                        .build()
                )
            }
            for (dbo in data) {
                operations.add(
                    ContentProviderOperation
                        .newInsert(uri)
                        .withValues(getCV(dbo))
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
        }
    }

    private fun mapAlbum(cursor: Cursor): VideoAlbumEntity {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(VideoAlbumsColumns.ALBUM_ID))
        val ownerId = cursor.getInt(cursor.getColumnIndexOrThrow(VideoAlbumsColumns.OWNER_ID))
        var privacyEntity: PrivacyEntity? = null
        val privacyJson = cursor.getString(cursor.getColumnIndexOrThrow(VideoAlbumsColumns.PRIVACY))
        if (privacyJson.nonNullNoEmpty()) {
            privacyEntity = GSON.fromJson(privacyJson, PrivacyEntity::class.java)
        }
        return VideoAlbumEntity(id, ownerId)
            .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(VideoAlbumsColumns.TITLE)))
            .setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow(VideoAlbumsColumns.UPDATE_TIME)))
            .setCount(cursor.getInt(cursor.getColumnIndexOrThrow(VideoAlbumsColumns.COUNT)))
            .setImage(cursor.getString(cursor.getColumnIndexOrThrow(VideoAlbumsColumns.IMAGE)))
            .setPrivacy(privacyEntity)
    }

    companion object {
        fun getCV(dbo: VideoAlbumEntity): ContentValues {
            val cv = ContentValues()
            cv.put(VideoAlbumsColumns.OWNER_ID, dbo.ownerId)
            cv.put(VideoAlbumsColumns.ALBUM_ID, dbo.id)
            cv.put(VideoAlbumsColumns.TITLE, dbo.title)
            cv.put(VideoAlbumsColumns.IMAGE, dbo.image)
            cv.put(VideoAlbumsColumns.COUNT, dbo.count)
            cv.put(VideoAlbumsColumns.UPDATE_TIME, dbo.updateTime)
            cv.put(
                VideoAlbumsColumns.PRIVACY,
                if (dbo.privacy != null) GSON.toJson(dbo.privacy) else null
            )
            return cv
        }
    }
}