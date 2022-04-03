package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getVideosContentUriFor
import dev.ragnarok.fenrir.db.column.VideoColumns
import dev.ragnarok.fenrir.db.interfaces.IVideoStorage
import dev.ragnarok.fenrir.db.model.entity.PrivacyEntity
import dev.ragnarok.fenrir.db.model.entity.VideoEntity
import dev.ragnarok.fenrir.model.VideoCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

internal class VideoStorage(base: AppStorages) : AbsStorage(base), IVideoStorage {
    override fun findByCriteria(criteria: VideoCriteria): Single<List<VideoEntity>> {
        return Single.create { e: SingleEmitter<List<VideoEntity>> ->
            val uri = getVideosContentUriFor(criteria.accountId)
            val where: String
            val args: Array<String>
            val range = criteria.range
            when {
                range != null -> {
                    where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?"
                    args = arrayOf(range.first.toString(), range.last.toString())
                }
                criteria.albumId == 0 -> {
                    where = VideoColumns.OWNER_ID + " = ?"
                    args = arrayOf(criteria.ownerId.toString())
                }
                else -> {
                    where = VideoColumns.OWNER_ID + " = ? AND " + VideoColumns.ALBUM_ID + " = ?"
                    args = arrayOf(criteria.ownerId.toString(), criteria.albumId.toString())
                }
            }
            val cursor =
                contentResolver.query(uri, null, where, args, VideoColumns.ADDING_DATE + " DESC")
            val videos = ArrayList<VideoEntity>(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    videos.add(mapVideo(cursor))
                }
                cursor.close()
            }
            e.onSuccess(videos)
        }
    }

    private fun mapVideo(cursor: Cursor): VideoEntity {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.VIDEO_ID))
        val ownerId = cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.ORIGINAL_OWNER_ID))
        val video = VideoEntity().set(id, ownerId)
            .setAlbumId(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.ALBUM_ID)))
            .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.TITLE)))
            .setDescription(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.DESCRIPTION)))
            .setLink(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.LINK)))
            .setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.DURATION)))
            .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(VideoColumns.DATE)))
            .setAddingDate(cursor.getLong(cursor.getColumnIndexOrThrow(VideoColumns.ADDING_DATE)))
            .setViews(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.VIEWS)))
            .setPlayer(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.PLAYER)))
            .setImage(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.IMAGE)))
            .setAccessKey(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.ACCESS_KEY)))
            .setCommentsCount(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.COMMENTS)))
            .setCanComment(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.CAN_COMENT)) == 1)
            .setCanRepost(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.CAN_REPOST)) == 1)
            .setUserLikes(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.USER_LIKES)) == 1)
            .setLikesCount(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.LIKES)))
            .setRepeat(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.REPEAT)) == 1)
            .setMp4link240(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.MP4_240)))
            .setMp4link360(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.MP4_360)))
            .setMp4link480(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.MP4_480)))
            .setMp4link720(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.MP4_720)))
            .setMp4link1080(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.MP4_1080)))
            .setExternalLink(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.EXTERNAL)))
            .setHls(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.HLS)))
            .setLive(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.LIVE)))
            .setPlatform(cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.PLATFORM)))
            .setCanEdit(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.CAN_EDIT)) == 1)
            .setCanAdd(cursor.getInt(cursor.getColumnIndexOrThrow(VideoColumns.CAN_ADD)) == 1)
        val privacyViewText =
            cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.PRIVACY_VIEW))
        if (privacyViewText.nonNullNoEmpty()) {
            video.privacyView = GSON.fromJson(privacyViewText, PrivacyEntity::class.java)
        }
        val privacyCommentText =
            cursor.getString(cursor.getColumnIndexOrThrow(VideoColumns.PRIVACY_COMMENT))
        if (privacyCommentText.nonNullNoEmpty()) {
            video.privacyComment =
                GSON.fromJson(privacyCommentText, PrivacyEntity::class.java)
        }
        return video
    }

    override fun insertData(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        videos: List<VideoEntity>,
        invalidateBefore: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val operations = ArrayList<ContentProviderOperation>()
            val uri = getVideosContentUriFor(accountId)
            if (invalidateBefore) {
                if (albumId == 0) {
                    operations.add(
                        ContentProviderOperation
                            .newDelete(uri)
                            .withSelection(
                                VideoColumns.OWNER_ID + " = ?",
                                arrayOf(ownerId.toString())
                            )
                            .build()
                    )
                } else {
                    operations.add(
                        ContentProviderOperation
                            .newDelete(uri)
                            .withSelection(
                                VideoColumns.OWNER_ID + " = ? AND " + VideoColumns.ALBUM_ID + " = ?",
                                arrayOf(ownerId.toString(), albumId.toString())
                            )
                            .build()
                    )
                }
            }
            for (dbo in videos) {
                val cv = getCV(dbo, ownerId)
                cv.put(VideoColumns.ALBUM_ID, albumId)
                operations.add(
                    ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
        }
    }

    companion object {
        /* Дело в том, что вк передает в p.owner_id идентификатор оригинального владельца.
     * Поэтому необходимо отдельно сохранять идентикатор owner-а, у кого в видеозаписях мы нашли видео */
        fun getCV(dbo: VideoEntity, ownerId: Int): ContentValues {
            val cv = ContentValues()
            cv.put(VideoColumns.VIDEO_ID, dbo.id)
            cv.put(VideoColumns.OWNER_ID, ownerId)
            cv.put(VideoColumns.ORIGINAL_OWNER_ID, dbo.ownerId)
            cv.put(VideoColumns.ALBUM_ID, dbo.albumId)
            cv.put(VideoColumns.TITLE, dbo.title)
            cv.put(VideoColumns.DESCRIPTION, dbo.description)
            cv.put(VideoColumns.DURATION, dbo.duration)
            cv.put(VideoColumns.LINK, dbo.link)
            cv.put(VideoColumns.DATE, dbo.date)
            cv.put(VideoColumns.ADDING_DATE, dbo.addingDate)
            cv.put(VideoColumns.VIEWS, dbo.views)
            cv.put(VideoColumns.PLAYER, dbo.player)
            cv.put(VideoColumns.IMAGE, dbo.image)
            cv.put(VideoColumns.ACCESS_KEY, dbo.accessKey)
            cv.put(VideoColumns.COMMENTS, dbo.commentsCount)
            cv.put(VideoColumns.CAN_COMENT, dbo.isCanComment)
            cv.put(VideoColumns.CAN_REPOST, dbo.isCanRepost)
            cv.put(VideoColumns.USER_LIKES, dbo.isUserLikes)
            cv.put(VideoColumns.REPEAT, dbo.isRepeat)
            cv.put(VideoColumns.LIKES, dbo.likesCount)
            cv.put(
                VideoColumns.PRIVACY_VIEW,
                if (dbo.privacyView != null) GSON.toJson(dbo.privacyView) else null
            )
            cv.put(
                VideoColumns.PRIVACY_COMMENT,
                if (dbo.privacyComment != null) GSON.toJson(dbo.privacyComment) else null
            )
            cv.put(VideoColumns.MP4_240, dbo.mp4link240)
            cv.put(VideoColumns.MP4_360, dbo.mp4link360)
            cv.put(VideoColumns.MP4_480, dbo.mp4link480)
            cv.put(VideoColumns.MP4_720, dbo.mp4link720)
            cv.put(VideoColumns.MP4_1080, dbo.mp4link1080)
            cv.put(VideoColumns.EXTERNAL, dbo.externalLink)
            cv.put(VideoColumns.HLS, dbo.hls)
            cv.put(VideoColumns.LIVE, dbo.live)
            cv.put(VideoColumns.PLATFORM, dbo.platform)
            cv.put(VideoColumns.CAN_EDIT, dbo.isCanEdit)
            cv.put(VideoColumns.CAN_ADD, dbo.isCanAdd)
            return cv
        }
    }
}