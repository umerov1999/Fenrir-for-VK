package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getPhotosContentUriFor
import dev.ragnarok.fenrir.db.column.PhotosColumns
import dev.ragnarok.fenrir.db.interfaces.IPhotosStorage
import dev.ragnarok.fenrir.db.model.PhotoPatch
import dev.ragnarok.fenrir.db.model.entity.PhotoDboEntity
import dev.ragnarok.fenrir.db.model.entity.PhotoSizeEntity
import dev.ragnarok.fenrir.model.criteria.PhotoCriteria
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import dev.ragnarok.fenrir.util.msgpack.MsgPack
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class PhotosStorage(base: AppStorages) : AbsStorage(base), IPhotosStorage {
    override fun insertPhotosRx(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        photos: List<PhotoDboEntity>,
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

    override fun findPhotosByCriteriaRx(criteria: PhotoCriteria): Single<List<PhotoDboEntity>> {
        return Single.create { e: SingleEmitter<List<PhotoDboEntity>> ->
            val selection = getSelectionForCriteria(criteria)
            val orderBy =
                if (criteria.orderBy == null) PhotosColumns.PHOTO_ID + (if (!criteria.sortInvert) " DESC" else " ASC") else criteria.orderBy
            val uri = getPhotosContentUriFor(criteria.accountId)
            val cursor = context.contentResolver.query(uri, null, selection, null, orderBy)
            val photos = ArrayList<PhotoDboEntity>(safeCountOf(cursor))
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
            patch.like.requireNonNull {
                cv.put(PhotosColumns.LIKES, it.count)
                cv.put(PhotosColumns.USER_LIKES, it.isLiked)
            }
            patch.deletion.requireNonNull {
                cv.put(PhotosColumns.DELETED, it.isDeleted)
            }
            if (cv.size() > 0) {
                val uri = getPhotosContentUriFor(accountId)
                val where = PhotosColumns.PHOTO_ID + " = ? AND " + PhotosColumns.OWNER_ID + " = ?"
                val args = arrayOf(photoId.toString(), ownerId.toString())
                contentResolver.update(uri, cv, where, args)
            }
        }
    }

    private fun mapPhotoDbo(cursor: Cursor): PhotoDboEntity {
        var sizes: PhotoSizeEntity? = null
        val sizesJson = cursor.getBlob(PhotosColumns.SIZES)
        if (sizesJson.nonNullNoEmpty()) {
            sizes = MsgPack.decodeFromByteArray(PhotoSizeEntity.serializer(), sizesJson)
        }
        val id = cursor.getInt(PhotosColumns.PHOTO_ID)
        val ownerId = cursor.getInt(PhotosColumns.OWNER_ID)
        return PhotoDboEntity().set(id, ownerId)
            .setSizes(sizes)
            .setAlbumId(cursor.getInt(PhotosColumns.ALBUM_ID))
            .setWidth(cursor.getInt(PhotosColumns.WIDTH))
            .setHeight(cursor.getInt(PhotosColumns.HEIGHT))
            .setText(cursor.getString(PhotosColumns.TEXT))
            .setDate(cursor.getLong(PhotosColumns.DATE))
            .setUserLikes(cursor.getBoolean(PhotosColumns.USER_LIKES))
            .setCanComment(cursor.getBoolean(PhotosColumns.CAN_COMMENT))
            .setLikesCount(cursor.getInt(PhotosColumns.LIKES))
            .setCommentsCount(cursor.getInt(PhotosColumns.COMMENTS))
            .setTagsCount(cursor.getInt(PhotosColumns.TAGS))
            .setAccessKey(cursor.getString(PhotosColumns.ACCESS_KEY))
            .setDeleted(cursor.getBoolean(PhotosColumns.DELETED))
    }

    companion object {
        private fun getCV(dbo: PhotoDboEntity): ContentValues {
            val cv = ContentValues()
            cv.put(PhotosColumns.PHOTO_ID, dbo.id)
            cv.put(PhotosColumns.ALBUM_ID, dbo.albumId)
            cv.put(PhotosColumns.OWNER_ID, dbo.ownerId)
            cv.put(PhotosColumns.WIDTH, dbo.width)
            cv.put(PhotosColumns.HEIGHT, dbo.height)
            cv.put(PhotosColumns.TEXT, dbo.text)
            cv.put(PhotosColumns.DATE, dbo.date)
            dbo.sizes.ifNonNull({
                cv.put(
                    PhotosColumns.SIZES,
                    MsgPack.encodeToByteArray(PhotoSizeEntity.serializer(), it)
                )
            }, {
                cv.putNull(PhotosColumns.SIZES)
            })
            cv.put(PhotosColumns.USER_LIKES, dbo.isUserLikes)
            cv.put(PhotosColumns.CAN_COMMENT, dbo.isCanComment)
            cv.put(PhotosColumns.LIKES, dbo.likesCount)
            cv.put(PhotosColumns.REPOSTS, dbo.repostsCount)
            cv.put(PhotosColumns.COMMENTS, dbo.commentsCount)
            cv.put(PhotosColumns.TAGS, dbo.tagsCount)
            cv.put(PhotosColumns.ACCESS_KEY, dbo.accessKey)
            cv.put(PhotosColumns.DELETED, dbo.isDeleted)
            return cv
        }

        private fun getSelectionForCriteria(criteria: PhotoCriteria): String {
            var selection = "1 = 1"
            selection = selection + " AND " + PhotosColumns.OWNER_ID + " = " + criteria.ownerId
            selection = selection + " AND " + PhotosColumns.ALBUM_ID + " = " + criteria.albumId
            val range = criteria.range
            if (range != null) {
                selection = selection + " AND " + BaseColumns._ID + " >= " + range.first +
                        " AND " + BaseColumns._ID + " <= " + criteria.range?.last.orZero()
            }
            return selection
        }
    }
}