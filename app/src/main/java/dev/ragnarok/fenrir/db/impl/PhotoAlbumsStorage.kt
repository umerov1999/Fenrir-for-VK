package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getPhotoAlbumsContentUriFor
import dev.ragnarok.fenrir.db.column.PhotoAlbumsColumns
import dev.ragnarok.fenrir.db.interfaces.IPhotoAlbumsStorage
import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumEntity
import dev.ragnarok.fenrir.db.model.entity.PhotoSizeEntity
import dev.ragnarok.fenrir.db.model.entity.PrivacyEntity
import dev.ragnarok.fenrir.model.criteria.PhotoAlbumsCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class PhotoAlbumsStorage(base: AppStorages) : AbsStorage(base), IPhotoAlbumsStorage {
    override fun findAlbumById(
        accountId: Int,
        ownerId: Int,
        albumId: Int
    ): Single<Optional<PhotoAlbumEntity>> {
        return Single.create { e: SingleEmitter<Optional<PhotoAlbumEntity>> ->
            val where =
                PhotoAlbumsColumns.OWNER_ID + " = ? AND " + PhotoAlbumsColumns.ALBUM_ID + " = ?"
            val args = arrayOf(ownerId.toString(), albumId.toString())
            val uri = getPhotoAlbumsContentUriFor(accountId)
            val cursor = context.contentResolver.query(uri, null, where, args, null)
            var album: PhotoAlbumEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    album = mapAlbum(cursor)
                }
                cursor.close()
            }
            e.onSuccess(wrap(album))
        }
    }

    override fun findAlbumsByCriteria(criteria: PhotoAlbumsCriteria): Single<List<PhotoAlbumEntity>> {
        return Single.create { e: SingleEmitter<List<PhotoAlbumEntity>> ->
            val uri = getPhotoAlbumsContentUriFor(criteria.accountId)
            val cursor = context.contentResolver.query(
                uri,
                null,
                PhotoAlbumsColumns.OWNER_ID + " = ?",
                arrayOf(criteria.ownerId.toString()),
                null
            )
            val data: MutableList<PhotoAlbumEntity> = ArrayList(safeCountOf(cursor))
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

    override fun store(
        accountId: Int,
        ownerId: Int,
        albums: List<PhotoAlbumEntity>,
        clearBefore: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val operations =
                ArrayList<ContentProviderOperation>(if (clearBefore) albums.size + 1 else albums.size)
            val uri = getPhotoAlbumsContentUriFor(accountId)
            if (clearBefore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .withSelection(
                            PhotoAlbumsColumns.OWNER_ID + " = ?",
                            arrayOf(ownerId.toString())
                        )
                        .build()
                )
            }
            for (dbo in albums) {
                operations.add(
                    ContentProviderOperation
                        .newInsert(uri)
                        .withValues(createCv(dbo))
                        .build()
                )
            }
            context.contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
        }
    }

    override fun removeAlbumById(accountId: Int, ownerId: Int, albumId: Int): Completable {
        return Completable.create { e: CompletableEmitter ->
            val where =
                PhotoAlbumsColumns.OWNER_ID + " = ? AND " + PhotoAlbumsColumns.ALBUM_ID + " = ?"
            val args = arrayOf(ownerId.toString(), albumId.toString())
            val uri = getPhotoAlbumsContentUriFor(accountId)
            context.contentResolver.delete(uri, where, args)
            e.onComplete()
        }
    }

    private fun mapAlbum(cursor: Cursor): PhotoAlbumEntity {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.ALBUM_ID))
        val ownerId = cursor.getInt(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.OWNER_ID))
        val album = PhotoAlbumEntity().set(id, ownerId)
            .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.TITLE)))
            .setSize(cursor.getInt(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.SIZE)))
            .setDescription(cursor.getString(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.DESCRIPTION)))
            .setCanUpload(cursor.getInt(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.CAN_UPLOAD)) == 1)
            .setUpdatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.UPDATED)))
            .setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.CREATED)))
            .setUploadByAdminsOnly(cursor.getInt(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.UPLOAD_BY_ADMINS)) == 1)
            .setCommentsDisabled(cursor.getInt(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.COMMENTS_DISABLED)) == 1)
        val sizesJson = cursor.getString(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.SIZES))
        if (sizesJson.nonNullNoEmpty()) {
            album.sizes = GSON.fromJson(sizesJson, PhotoSizeEntity::class.java)
        }
        val privacyViewText =
            cursor.getString(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.PRIVACY_VIEW))
        if (privacyViewText.nonNullNoEmpty()) {
            album.privacyView = GSON.fromJson(privacyViewText, PrivacyEntity::class.java)
        }
        val privacyCommentText =
            cursor.getString(cursor.getColumnIndexOrThrow(PhotoAlbumsColumns.PRIVACY_COMMENT))
        if (privacyCommentText.nonNullNoEmpty()) {
            album.privacyComment =
                GSON.fromJson(privacyCommentText, PrivacyEntity::class.java)
        }
        return album
    }

    companion object {
        private fun createCv(dbo: PhotoAlbumEntity): ContentValues {
            val cv = ContentValues()
            cv.put(PhotoAlbumsColumns.ALBUM_ID, dbo.id)
            cv.put(PhotoAlbumsColumns.OWNER_ID, dbo.ownerId)
            cv.put(PhotoAlbumsColumns.TITLE, dbo.title)
            cv.put(PhotoAlbumsColumns.SIZE, dbo.size)
            cv.put(
                PhotoAlbumsColumns.PRIVACY_VIEW,
                if (dbo.privacyView != null) GSON.toJson(dbo.privacyView) else null
            )
            cv.put(
                PhotoAlbumsColumns.PRIVACY_COMMENT,
                if (dbo.privacyComment != null) GSON.toJson(dbo.privacyComment) else null
            )
            cv.put(PhotoAlbumsColumns.DESCRIPTION, dbo.description)
            cv.put(PhotoAlbumsColumns.CAN_UPLOAD, dbo.isCanUpload)
            cv.put(PhotoAlbumsColumns.UPDATED, dbo.updatedTime)
            cv.put(PhotoAlbumsColumns.CREATED, dbo.createdTime)
            if (dbo.sizes != null) {
                cv.put(PhotoAlbumsColumns.SIZES, GSON.toJson(dbo.sizes))
            } else {
                cv.putNull(PhotoAlbumsColumns.SIZES)
            }
            cv.put(PhotoAlbumsColumns.UPLOAD_BY_ADMINS, dbo.isUploadByAdminsOnly)
            cv.put(PhotoAlbumsColumns.COMMENTS_DISABLED, dbo.isCommentsDisabled)
            return cv
        }
    }
}