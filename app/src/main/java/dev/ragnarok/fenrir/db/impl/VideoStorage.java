package dev.ragnarok.fenrir.db.impl;

import static android.provider.BaseColumns._ID;
import static dev.ragnarok.fenrir.db.column.VideoColumns.ACCESS_KEY;
import static dev.ragnarok.fenrir.db.column.VideoColumns.ADDING_DATE;
import static dev.ragnarok.fenrir.db.column.VideoColumns.ALBUM_ID;
import static dev.ragnarok.fenrir.db.column.VideoColumns.CAN_COMENT;
import static dev.ragnarok.fenrir.db.column.VideoColumns.CAN_REPOST;
import static dev.ragnarok.fenrir.db.column.VideoColumns.COMMENTS;
import static dev.ragnarok.fenrir.db.column.VideoColumns.DATE;
import static dev.ragnarok.fenrir.db.column.VideoColumns.DESCRIPTION;
import static dev.ragnarok.fenrir.db.column.VideoColumns.DURATION;
import static dev.ragnarok.fenrir.db.column.VideoColumns.EXTERNAL;
import static dev.ragnarok.fenrir.db.column.VideoColumns.HLS;
import static dev.ragnarok.fenrir.db.column.VideoColumns.IMAGE;
import static dev.ragnarok.fenrir.db.column.VideoColumns.LIKES;
import static dev.ragnarok.fenrir.db.column.VideoColumns.LINK;
import static dev.ragnarok.fenrir.db.column.VideoColumns.LIVE;
import static dev.ragnarok.fenrir.db.column.VideoColumns.MP4_1080;
import static dev.ragnarok.fenrir.db.column.VideoColumns.MP4_240;
import static dev.ragnarok.fenrir.db.column.VideoColumns.MP4_360;
import static dev.ragnarok.fenrir.db.column.VideoColumns.MP4_480;
import static dev.ragnarok.fenrir.db.column.VideoColumns.MP4_720;
import static dev.ragnarok.fenrir.db.column.VideoColumns.ORIGINAL_OWNER_ID;
import static dev.ragnarok.fenrir.db.column.VideoColumns.OWNER_ID;
import static dev.ragnarok.fenrir.db.column.VideoColumns.PLAYER;
import static dev.ragnarok.fenrir.db.column.VideoColumns.PRIVACY_COMMENT;
import static dev.ragnarok.fenrir.db.column.VideoColumns.PRIVACY_VIEW;
import static dev.ragnarok.fenrir.db.column.VideoColumns.REPEAT;
import static dev.ragnarok.fenrir.db.column.VideoColumns.TITLE;
import static dev.ragnarok.fenrir.db.column.VideoColumns.USER_LIKES;
import static dev.ragnarok.fenrir.db.column.VideoColumns.VIDEO_ID;
import static dev.ragnarok.fenrir.db.column.VideoColumns.VIEWS;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.DatabaseIdRange;
import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.VideoColumns;
import dev.ragnarok.fenrir.db.interfaces.IVideoStorage;
import dev.ragnarok.fenrir.db.model.entity.PrivacyEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoEntity;
import dev.ragnarok.fenrir.model.VideoCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


class VideoStorage extends AbsStorage implements IVideoStorage {

    VideoStorage(@NonNull AppStorages base) {
        super(base);
    }

    /* Дело в том, что вк передает в p.owner_id идентификатор оригинального владельца.
     * Поэтому необходимо отдельно сохранять идентикатор owner-а, у кого в видеозаписях мы нашли видео */
    public static ContentValues getCV(VideoEntity dbo, int ownerId) {
        ContentValues cv = new ContentValues();
        cv.put(VIDEO_ID, dbo.getId());
        cv.put(OWNER_ID, ownerId);
        cv.put(ORIGINAL_OWNER_ID, dbo.getOwnerId());
        cv.put(ALBUM_ID, dbo.getAlbumId());
        cv.put(TITLE, dbo.getTitle());
        cv.put(DESCRIPTION, dbo.getDescription());
        cv.put(DURATION, dbo.getDuration());
        cv.put(LINK, dbo.getLink());
        cv.put(DATE, dbo.getDate());
        cv.put(ADDING_DATE, dbo.getAddingDate());
        cv.put(VIEWS, dbo.getViews());
        cv.put(PLAYER, dbo.getPlayer());
        cv.put(IMAGE, dbo.getImage());
        cv.put(ACCESS_KEY, dbo.getAccessKey());
        cv.put(COMMENTS, dbo.getCommentsCount());
        cv.put(CAN_COMENT, dbo.isCanComment());
        cv.put(CAN_REPOST, dbo.isCanRepost());
        cv.put(USER_LIKES, dbo.isUserLikes());
        cv.put(REPEAT, dbo.isRepeat());
        cv.put(LIKES, dbo.getLikesCount());
        cv.put(PRIVACY_VIEW, nonNull(dbo.getPrivacyView()) ? GSON.toJson(dbo.getPrivacyView()) : null);
        cv.put(PRIVACY_COMMENT, nonNull(dbo.getPrivacyComment()) ? GSON.toJson(dbo.getPrivacyComment()) : null);
        cv.put(MP4_240, dbo.getMp4link240());
        cv.put(MP4_360, dbo.getMp4link360());
        cv.put(MP4_480, dbo.getMp4link480());
        cv.put(MP4_720, dbo.getMp4link720());
        cv.put(MP4_1080, dbo.getMp4link1080());
        cv.put(EXTERNAL, dbo.getExternalLink());
        cv.put(HLS, dbo.getHls());
        cv.put(LIVE, dbo.getLive());
        cv.put(VideoColumns.PLATFORM, dbo.getPlatform());
        cv.put(VideoColumns.CAN_EDIT, dbo.isCanEdit());
        cv.put(VideoColumns.CAN_ADD, dbo.isCanAdd());
        return cv;
    }

    @Override
    public Single<List<VideoEntity>> findByCriteria(@NonNull VideoCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getVideosContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            DatabaseIdRange range = criteria.getRange();
            if (nonNull(range)) {
                where = _ID + " >= ? AND " + _ID + " <= ?";
                args = new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())};
            } else if (criteria.getAlbumId() == 0) {
                where = OWNER_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId())};
            } else {
                where = OWNER_ID + " = ? AND " + ALBUM_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId()), String.valueOf(criteria.getAlbumId())};
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, ADDING_DATE + " DESC");

            ArrayList<VideoEntity> videos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    videos.add(mapVideo(cursor));
                }

                cursor.close();
            }

            e.onSuccess(videos);
        });
    }

    private VideoEntity mapVideo(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(VIDEO_ID));
        int ownerId = cursor.getInt(cursor.getColumnIndex(ORIGINAL_OWNER_ID));

        VideoEntity video = new VideoEntity().set(id, ownerId)
                .setAlbumId(cursor.getInt(cursor.getColumnIndex(ALBUM_ID)))
                .setTitle(cursor.getString(cursor.getColumnIndex(TITLE)))
                .setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)))
                .setLink(cursor.getString(cursor.getColumnIndex(LINK)))
                .setDuration(cursor.getInt(cursor.getColumnIndex(DURATION)))
                .setDate(cursor.getLong(cursor.getColumnIndex(DATE)))
                .setAddingDate(cursor.getLong(cursor.getColumnIndex(ADDING_DATE)))
                .setViews(cursor.getInt(cursor.getColumnIndex(VIEWS)))
                .setPlayer(cursor.getString(cursor.getColumnIndex(PLAYER)))
                .setImage(cursor.getString(cursor.getColumnIndex(IMAGE)))
                .setAccessKey(cursor.getString(cursor.getColumnIndex(ACCESS_KEY)))
                .setCommentsCount(cursor.getInt(cursor.getColumnIndex(COMMENTS)))
                .setCanComment(cursor.getInt(cursor.getColumnIndex(CAN_COMENT)) == 1)
                .setCanRepost(cursor.getInt(cursor.getColumnIndex(CAN_REPOST)) == 1)
                .setUserLikes(cursor.getInt(cursor.getColumnIndex(USER_LIKES)) == 1)
                .setLikesCount(cursor.getInt(cursor.getColumnIndex(LIKES)))
                .setRepeat(cursor.getInt(cursor.getColumnIndex(REPEAT)) == 1)
                .setMp4link240(cursor.getString(cursor.getColumnIndex(MP4_240)))
                .setMp4link360(cursor.getString(cursor.getColumnIndex(MP4_360)))
                .setMp4link480(cursor.getString(cursor.getColumnIndex(MP4_480)))
                .setMp4link720(cursor.getString(cursor.getColumnIndex(MP4_720)))
                .setMp4link1080(cursor.getString(cursor.getColumnIndex(MP4_1080)))
                .setExternalLink(cursor.getString(cursor.getColumnIndex(EXTERNAL)))
                .setHls(cursor.getString(cursor.getColumnIndex(HLS)))
                .setLive(cursor.getString(cursor.getColumnIndex(LIVE)))
                .setPlatform(cursor.getString(cursor.getColumnIndex(VideoColumns.PLATFORM)))
                .setCanEdit(cursor.getInt(cursor.getColumnIndex(VideoColumns.CAN_EDIT)) == 1)
                .setCanAdd(cursor.getInt(cursor.getColumnIndex(VideoColumns.CAN_ADD)) == 1);

        String privacyViewText = cursor.getString(cursor.getColumnIndex(PRIVACY_VIEW));
        if (nonEmpty(privacyViewText)) {
            video.setPrivacyView(GSON.fromJson(privacyViewText, PrivacyEntity.class));
        }

        String privacyCommentText = cursor.getString(cursor.getColumnIndex(PRIVACY_COMMENT));
        if (nonEmpty(privacyCommentText)) {
            video.setPrivacyComment(GSON.fromJson(privacyCommentText, PrivacyEntity.class));
        }

        return video;
    }

    @Override
    public Completable insertData(int accountId, int ownerId, int albumId, List<VideoEntity> videos, boolean deleteBeforeInsert) {
        return Completable.create(e -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            Uri uri = MessengerContentProvider.getVideosContentUriFor(accountId);

            if (deleteBeforeInsert) {
                if (albumId == 0) {
                    operations.add(ContentProviderOperation
                            .newDelete(uri)
                            .withSelection(OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)})
                            .build());
                } else {
                    operations.add(ContentProviderOperation
                            .newDelete(uri)
                            .withSelection(OWNER_ID + " = ? AND " + ALBUM_ID + " = ?",
                                    new String[]{String.valueOf(ownerId), String.valueOf(albumId)})
                            .build());
                }
            }

            for (VideoEntity dbo : videos) {
                ContentValues cv = getCV(dbo, ownerId);
                cv.put(ALBUM_ID, albumId);

                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }
}
