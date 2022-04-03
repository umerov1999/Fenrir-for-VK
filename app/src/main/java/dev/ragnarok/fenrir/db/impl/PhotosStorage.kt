package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getPhotosContentUriFor
import dev.ragnarok.fenrir.db.column.PhotosColumns
import dev.ragnarok.fenrir.db.interfaces.IPhotosStorage
import dev.ragnarok.fenrir.db.model.PhotoPatch
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity
import dev.ragnarok.fenrir.db.model.entity.PhotoSizeEntity
import dev.ragnarok.fenrir.model.criteria.PhotoCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class PhotosStorage(base: AppStorages) : AbsStorage(base), IPhotosStorage {
    override fun insertPhotosRx(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        photos: List<PhotoEntity>,
        clearBefore: Boolean
    ): Completable {
        return Completable.fromAction {
            val operations =
                ArrayList<ContentProviderOperation>(if (clearBefore) photos.size + 1 else photos.size)
            val uri = getPhotosContentUriFor(accountId)
            if (clearBefore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .withSelection(
                            PhotosColumns.OWNER_ID + " = ? AND " + PhotosColumns.ALBUM_ID + " = ?",
                            arrayOf(ownerId.toString(), albumId.toString())
                        )
                        .build()
                )
            }
            for (dbo in photos) {
                operations.add(
                    ContentProviderOperation
                        .newInsert(uri)
                        .withValues(getCV(dbo))
                        .build()
                )
            }
            context.contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
        }
    }

    override fun findPhotosByCriteriaRx(criteria: PhotoCriteria): Single<List<PhotoEntity>> {
        return Single.create { e: SingleEmitter<List<PhotoEntity>> ->
            val selection = getSelectionForCriteria(criteria)
            val orderBy =
                if (criteria.orderBy == null) PhotosColumns.PHOTO_ID + (if (!criteria.sortInvert) " DESC" else " ASC") else criteria.orderBy
            val uri = getPhotosContentUriFor(criteria.accountId)
            val cursor = context.contentResolver.query(uri, null, selection, null, orderBy)
            val photos = ArrayList<PhotoEntity>(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    photos.add(mapPhotoDbo(cursor))
                }
                cursor.close()
            }
            e.onSuccess(photos)
        }
    }

    override fun applyPatch(
        accountId: Int,
        ownerId: Int,
        photoId: Int,
        patch: PhotoPatch
    ): Completable {
        return Completable.fromAction {
            val cv = ContentValues()
            if (patch.like != null) {
                cv.put(PhotosColumns.LIKES, patch.like.count)
                cv.put(PhotosColumns.USER_LIKES, patch.like.isLiked)
            }
            if (patch.deletion != null) {
                cv.put(PhotosColumns.DELETED, patch.deletion.isDeleted)
            }
            if (cv.size() > 0) {
                val uri = getPhotosContentUriFor(accountId)
                val where = PhotosColumns.PHOTO_ID + " = ? AND " + PhotosColumns.OWNER_ID + " = ?"
                val args = arrayOf(photoId.toString(), ownerId.toString())
                contentResolver.update(uri, cv, where, args)
            }
        }
    }

    private fun mapPhotoDbo(cursor: Cursor): PhotoEntity {
        var sizes: PhotoSizeEntity? = null
        val sizesJson = cursor.getString(cursor.getColumnIndexOrThrow(PhotosColumns.SIZES))
        if (sizesJson.nonNullNoEmpty()) {
            sizes = GSON.fromJson(sizesJson, PhotoSizeEntity::class.java)
        }
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.PHOTO_ID))
        val ownerId = cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.OWNER_ID))
        return PhotoEntity().set(id, ownerId)
            .setSizes(sizes)
            .setAlbumId(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.ALBUM_ID)))
            .setWidth(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.WIDTH)))
            .setHeight(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.HEIGHT)))
            .setText(cursor.getString(cursor.getColumnIndexOrThrow(PhotosColumns.TEXT)))
            .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(PhotosColumns.DATE)))
            .setUserLikes(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.USER_LIKES)) == 1)
            .setCanComment(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.CAN_COMMENT)) == 1)
            .setLikesCount(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.LIKES)))
            .setCommentsCount(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.COMMENTS)))
            .setTagsCount(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.TAGS)))
            .setAccessKey(cursor.getString(cursor.getColumnIndexOrThrow(PhotosColumns.ACCESS_KEY)))
            .setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(PhotosColumns.DELETED)) == 1)
    }

    companion object {
        private fun getCV(dbo: PhotoEntity): ContentValues {
            val cv = ContentValues()
            cv.put(PhotosColumns.PHOTO_ID, dbo.id)
            cv.put(PhotosColumns.ALBUM_ID, dbo.albumId)
            cv.put(PhotosColumns.OWNER_ID, dbo.ownerId)
            cv.put(PhotosColumns.WIDTH, dbo.width)
            cv.put(PhotosColumns.HEIGHT, dbo.height)
            cv.put(PhotosColumns.TEXT, dbo.text)
            cv.put(PhotosColumns.DATE, dbo.date)
            if (dbo.sizes != null) {
                cv.put(PhotosColumns.SIZES, GSON.toJson(dbo.sizes))
            }
            cv.put(PhotosColumns.USER_LIKES, dbo.isUserLikes)
            cv.put(PhotosColumns.CAN_COMMENT, dbo.isCanComment)
            cv.put(PhotosColumns.LIKES, dbo.likesCount)
            cv.put(PhotosColumns.COMMENTS, dbo.commentsCount)
            cv.put(PhotosColumns.TAGS, dbo.tagsCount)
            cv.put(PhotosColumns.ACCESS_KEY, dbo.accessKey)
            cv.put(PhotosColumns.DELETED, dbo.isDeleted)
            return cv
        }

        private fun getSelectionForCriteria(criteria: PhotoCriteria): String {
            var selection = "1 = 1"
            if (criteria.ownerId != null) {
                selection = selection + " AND " + PhotosColumns.OWNER_ID + " = " + criteria.ownerId
            }
            if (criteria.albumId != null) {
                selection = selection + " AND " + PhotosColumns.ALBUM_ID + " = " + criteria.albumId
            }
            val range = criteria.range
            if (range != null) {
                selection = selection + " AND " + BaseColumns._ID + " >= " + range.first +
                        " AND " + BaseColumns._ID + " <= " + criteria.range.last
            }
            return selection
        }
    }
}