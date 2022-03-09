package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.db.impl.AttachmentsStorage.appendAttachOperationWithBackReference;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.db.DatabaseIdRange;
import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.CommentsColumns;
import dev.ragnarok.fenrir.db.interfaces.Cancelable;
import dev.ragnarok.fenrir.db.interfaces.ICommentsStorage;
import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.exception.DatabaseException;
import dev.ragnarok.fenrir.model.CommentUpdate;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.DraftComment;
import dev.ragnarok.fenrir.model.criteria.CommentsCriteria;
import dev.ragnarok.fenrir.util.Exestime;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Unixtime;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

class CommentsStorage extends AbsStorage implements ICommentsStorage {
    private static final Type THREADS_TYPE = new TypeToken<List<CommentEntity>>() {
    }.getType();
    private final PublishSubject<CommentUpdate> minorUpdatesPublisher;
    private final Object mStoreLock = new Object();

    CommentsStorage(@NonNull AppStorages base) {
        super(base);
        minorUpdatesPublisher = PublishSubject.create();
    }

    public static ContentValues getCV(int sourceId, int sourceOwnerId, int sourceType, CommentEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(CommentsColumns.COMMENT_ID, dbo.getId());
        cv.put(CommentsColumns.FROM_ID, dbo.getFromId());
        cv.put(CommentsColumns.DATE, dbo.getDate());
        cv.put(CommentsColumns.TEXT, dbo.getText());
        cv.put(CommentsColumns.REPLY_TO_USER, dbo.getReplyToUserId());
        cv.put(CommentsColumns.REPLY_TO_COMMENT, dbo.getReplyToComment());
        cv.put(CommentsColumns.THREADS_COUNT, dbo.getThreadsCount());
        if (nonEmpty(dbo.getThreads())) {
            cv.put(CommentsColumns.THREADS, GSON.toJson(dbo.getThreads()));
        } else {
            cv.putNull(CommentsColumns.THREADS);
        }
        cv.put(CommentsColumns.LIKES, dbo.getLikesCount());
        cv.put(CommentsColumns.USER_LIKES, dbo.isUserLikes());
        cv.put(CommentsColumns.CAN_LIKE, dbo.isCanLike());
        cv.put(CommentsColumns.ATTACHMENTS_COUNT, dbo.getAttachmentsCount());
        cv.put(CommentsColumns.SOURCE_ID, sourceId);
        cv.put(CommentsColumns.SOURCE_OWNER_ID, sourceOwnerId);
        cv.put(CommentsColumns.SOURCE_TYPE, sourceType);
        cv.put(CommentsColumns.DELETED, dbo.isDeleted());
        return cv;
    }

    @Override
    public Single<int[]> insert(int accountId, int sourceId, int sourceOwnerId, int sourceType, List<CommentEntity> dbos, OwnerEntities owners, boolean clearBefore) {
        return Single.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearBefore) {
                ContentProviderOperation delete = ContentProviderOperation
                        .newDelete(MessengerContentProvider.getCommentsContentUriFor(accountId))
                        .withSelection(CommentsColumns.SOURCE_ID + " = ? " +
                                        " AND " + CommentsColumns.SOURCE_OWNER_ID + " = ? " +
                                        " AND " + CommentsColumns.COMMENT_ID + " != ? " +
                                        " AND " + CommentsColumns.SOURCE_TYPE + " = ?",
                                new String[]{String.valueOf(sourceId),
                                        String.valueOf(sourceOwnerId),
                                        String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                                        String.valueOf(sourceType)}).build();
                operations.add(delete);
            }

            int[] indexes = new int[dbos.size()];
            for (int i = 0; i < dbos.size(); i++) {
                CommentEntity dbo = dbos.get(i);

                ContentProviderOperation mainPostHeaderOperation = ContentProviderOperation
                        .newInsert(MessengerContentProvider.getCommentsContentUriFor(accountId))
                        .withValues(getCV(sourceId, sourceOwnerId, sourceType, dbo))
                        .build();

                int mainPostHeaderIndex = addToListAndReturnIndex(operations, mainPostHeaderOperation);
                indexes[i] = mainPostHeaderIndex;

                if (!Utils.isEmpty(dbo.getAttachments())) {
                    for (Entity attachmentEntity : dbo.getAttachments()) {
                        appendAttachOperationWithBackReference(operations, accountId, AttachToType.COMMENT, mainPostHeaderIndex, attachmentEntity);
                    }
                }
            }

            if (nonNull(owners)) {
                OwnersStorage.appendOwnersInsertOperations(operations, accountId, owners);
            }

            ContentProviderResult[] results;
            synchronized (mStoreLock) {
                results = getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            }

            int[] ids = new int[dbos.size()];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];
                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            emitter.onSuccess(ids);
        });
    }

    private Cursor createCursorByCriteria(CommentsCriteria criteria) {
        Uri uri = MessengerContentProvider.getCommentsContentUriFor(criteria.getAccountId());

        DatabaseIdRange range = criteria.getRange();
        Commented commented = criteria.getCommented();

        if (Objects.isNull(range)) {
            return getContentResolver().query(uri, null,
                    CommentsColumns.SOURCE_ID + " = ? AND " +
                            CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                            CommentsColumns.SOURCE_TYPE + " = ? AND " +
                            CommentsColumns.COMMENT_ID + " != ?",
                    new String[]{String.valueOf(commented.getSourceId()),
                            String.valueOf(commented.getSourceOwnerId()),
                            String.valueOf(commented.getSourceType()),
                            String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID)},
                    CommentsColumns.COMMENT_ID + " DESC");
        } else {
            return getContentResolver().query(uri,
                    null, BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?",
                    new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())},
                    CommentsColumns.COMMENT_ID + " DESC");
        }
    }

    @Override
    public Single<List<CommentEntity>> getDbosByCriteria(@NonNull CommentsCriteria criteria) {
        return Single.create(emitter -> {
            Cursor cursor = createCursorByCriteria(criteria);

            Cancelable cancelation = emitter::isDisposed;
            List<CommentEntity> dbos = new ArrayList<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapDbo(criteria.getAccountId(), cursor, true, false, cancelation));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Maybe<DraftComment> findEditingComment(int accountId, @NonNull Commented commented) {
        return Maybe.<DraftComment>create(e -> {
            Cursor cursor = getContentResolver().query(
                    MessengerContentProvider.getCommentsContentUriFor(accountId), null,
                    CommentsColumns.COMMENT_ID + " = ? AND " +
                            CommentsColumns.SOURCE_ID + " = ? AND " +
                            CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                            CommentsColumns.SOURCE_TYPE + " = ?",
                    new String[]{
                            String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                            String.valueOf(commented.getSourceId()),
                            String.valueOf(commented.getSourceOwnerId()),
                            String.valueOf(commented.getSourceType())}, null);

            DraftComment comment = null;
            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    int dbid = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.TEXT));

                    comment = new DraftComment(dbid).setBody(body);
                }

                cursor.close();
            }

            if (nonNull(comment)) {
                e.onSuccess(comment);
            }

            e.onComplete();
        }).flatMap(comment -> getStores()
                .attachments()
                .getCount(accountId, AttachToType.COMMENT, comment.getId())
                .flatMapMaybe(count -> Maybe.just(comment.setAttachmentsCount(count))));
    }

    @Override
    public Single<Integer> saveDraftComment(int accountId, Commented commented, String text, int replyToUser, int replyToComment) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            Integer id = findEditingCommentId(accountId, commented);

            ContentValues contentValues = new ContentValues();
            contentValues.put(CommentsColumns.COMMENT_ID, CommentsColumns.PROCESSING_COMMENT_ID);
            contentValues.put(CommentsColumns.TEXT, text);
            contentValues.put(CommentsColumns.SOURCE_ID, commented.getSourceId());
            contentValues.put(CommentsColumns.SOURCE_OWNER_ID, commented.getSourceOwnerId());
            contentValues.put(CommentsColumns.SOURCE_TYPE, commented.getSourceType());
            contentValues.put(CommentsColumns.FROM_ID, accountId);
            contentValues.put(CommentsColumns.DATE, Unixtime.now());
            contentValues.put(CommentsColumns.REPLY_TO_USER, replyToUser);
            contentValues.put(CommentsColumns.REPLY_TO_COMMENT, replyToComment);
            contentValues.put(CommentsColumns.THREADS_COUNT, 0);
            contentValues.putNull(CommentsColumns.THREADS);
            contentValues.put(CommentsColumns.LIKES, 0);
            contentValues.put(CommentsColumns.USER_LIKES, 0);

            Uri commentsWithAccountUri = MessengerContentProvider.getCommentsContentUriFor(accountId);

            if (id == null) {
                Uri uri = getContentResolver().insert(commentsWithAccountUri, contentValues);

                if (uri == null) {
                    e.onError(new DatabaseException("Result URI is null"));
                    return;
                }

                id = Integer.parseInt(uri.getPathSegments().get(1));
            } else {
                getContentResolver().update(commentsWithAccountUri, contentValues,
                        BaseColumns._ID + " = ?", new String[]{String.valueOf(id)});
            }

            e.onSuccess(id);
            Exestime.log("CommentsStorage.saveDraftComment", start, "id: " + id);
        });
    }

    @Override
    public Completable commitMinorUpdate(CommentUpdate update) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();

            if (update.hasLikesUpdate()) {
                cv.put(CommentsColumns.USER_LIKES, update.getLikeUpdate().isUserLikes());
                cv.put(CommentsColumns.LIKES, update.getLikeUpdate().getCount());
            }

            if (update.hasDeleteUpdate()) {
                cv.put(CommentsColumns.DELETED, update.getDeleteUpdate().isDeleted());
            }

            Uri uri = MessengerContentProvider.getCommentsContentUriFor(update.getAccountId());

            String where = CommentsColumns.SOURCE_OWNER_ID + " = ? AND " + CommentsColumns.COMMENT_ID + " = ?";
            String[] args = {String.valueOf(update.getCommented().getSourceOwnerId()), String.valueOf(update.getCommentId())};

            getContentResolver().update(uri, cv, where, args);

            minorUpdatesPublisher.onNext(update);
        });
    }

    @Override
    public Observable<CommentUpdate> observeMinorUpdates() {
        return minorUpdatesPublisher;
    }

    @Override
    public Completable deleteByDbid(int accountId, Integer dbid) {
        return Completable.fromAction(() -> {
            Uri uri = MessengerContentProvider.getCommentsContentUriFor(accountId);
            String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(dbid)};
            getContentResolver().delete(uri, where, args);
        });
    }

    private Integer findEditingCommentId(int aid, Commented commented) {
        String[] projection = {BaseColumns._ID};
        Cursor cursor = getContentResolver().query(
                MessengerContentProvider.getCommentsContentUriFor(aid), projection,
                CommentsColumns.COMMENT_ID + " = ? AND " +
                        CommentsColumns.SOURCE_ID + " = ? AND " +
                        CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                        CommentsColumns.SOURCE_TYPE + " = ?",
                new String[]{
                        String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                        String.valueOf(commented.getSourceId()),
                        String.valueOf(commented.getSourceOwnerId()),
                        String.valueOf(commented.getSourceType())}, null);

        Integer result = null;

        if (nonNull(cursor)) {
            if (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }

            cursor.close();
        }
        return result;
    }

    private CommentEntity mapDbo(int accountId, Cursor cursor, boolean includeAttachments, boolean forceAttachments, Cancelable cancelable) {
        int attachmentsCount = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.ATTACHMENTS_COUNT));
        int dbid = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));

        int sourceId = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_ID));
        int sourceOwnerId = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_OWNER_ID));
        int sourceType = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_TYPE));
        String sourceAccessKey = cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.SOURCE_ACCESS_KEY));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.COMMENT_ID));

        String threadsJson = cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.THREADS));

        CommentEntity dbo = new CommentEntity().set(sourceId, sourceOwnerId, sourceType, sourceAccessKey, id)
                .setFromId(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.FROM_ID)))
                .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(CommentsColumns.DATE)))
                .setText(cursor.getString(cursor.getColumnIndexOrThrow(CommentsColumns.TEXT)))
                .setReplyToUserId(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.REPLY_TO_USER)))
                .setThreadsCount(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.THREADS_COUNT)))
                .setReplyToComment(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.REPLY_TO_COMMENT)))
                .setLikesCount(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.LIKES)))
                .setUserLikes(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.USER_LIKES)) == 1)
                .setCanLike(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.CAN_LIKE)) == 1)
                .setCanEdit(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.CAN_EDIT)) == 1)
                .setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(CommentsColumns.DELETED)) == 1);
        if (nonNull(threadsJson)) {
            dbo.setThreads(GSON.fromJson(threadsJson, THREADS_TYPE));
        }

        if (includeAttachments && (attachmentsCount > 0 || forceAttachments)) {
            dbo.setAttachments(getStores()
                    .attachments()
                    .getAttachmentsDbosSync(accountId, AttachToType.COMMENT, dbid, cancelable));
        }

        return dbo;
    }
}