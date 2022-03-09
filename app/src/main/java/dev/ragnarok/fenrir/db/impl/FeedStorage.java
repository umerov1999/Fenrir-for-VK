package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.join;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.ragnarok.fenrir.db.DatabaseIdRange;
import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.FeedListsColumns;
import dev.ragnarok.fenrir.db.column.NewsColumns;
import dev.ragnarok.fenrir.db.interfaces.IFeedStorage;
import dev.ragnarok.fenrir.db.model.entity.AttachmentsEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.FeedListEntity;
import dev.ragnarok.fenrir.db.model.entity.NewsEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.model.FeedSourceCriteria;
import dev.ragnarok.fenrir.model.criteria.FeedCriteria;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

class FeedStorage extends AbsStorage implements IFeedStorage {

    private final Object storeLock = new Object();

    FeedStorage(@NonNull AppStorages base) {
        super(base);
    }

    public static ContentValues getCV(NewsEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(NewsColumns.TYPE, dbo.getType());
        cv.put(NewsColumns.SOURCE_ID, dbo.getSourceId());
        cv.put(NewsColumns.DATE, dbo.getDate());
        cv.put(NewsColumns.POST_ID, dbo.getPostId());
        cv.put(NewsColumns.POST_TYPE, dbo.getPostType());
        cv.put(NewsColumns.FINAL_POST, dbo.isFinalPost());
        cv.put(NewsColumns.COPY_OWNER_ID, dbo.getCopyOwnerId());
        cv.put(NewsColumns.COPY_POST_ID, dbo.getCopyPostId());
        cv.put(NewsColumns.COPY_POST_DATE, dbo.getCopyPostDate());
        cv.put(NewsColumns.TEXT, dbo.getText());
        cv.put(NewsColumns.CAN_EDIT, dbo.isCanEdit());
        cv.put(NewsColumns.CAN_DELETE, dbo.isCanDelete());
        cv.put(NewsColumns.COMMENT_COUNT, dbo.getCommentCount());
        cv.put(NewsColumns.COMMENT_CAN_POST, dbo.isCanPostComment());
        cv.put(NewsColumns.LIKE_COUNT, dbo.getLikesCount());
        cv.put(NewsColumns.USER_LIKE, dbo.isUserLikes());
        cv.put(NewsColumns.CAN_LIKE, dbo.isCanLike());
        cv.put(NewsColumns.CAN_PUBLISH, dbo.isCanPublish());
        cv.put(NewsColumns.REPOSTS_COUNT, dbo.getRepostCount());
        cv.put(NewsColumns.USER_REPOSTED, dbo.isUserReposted());
        cv.put(NewsColumns.GEO_ID, dbo.getGeoId());
        cv.put(NewsColumns.TAG_FRIENDS, nonNull(dbo.getFriendsTags()) ? join(",", dbo.getFriendsTags()) : null);
        cv.put(NewsColumns.VIEWS, dbo.getViews());

        if (nonEmpty(dbo.getCopyHistory()) || nonEmpty(dbo.getAttachments())) {
            List<Entity> attachmentsEntities = new ArrayList<>();

            if (nonEmpty(dbo.getAttachments())) {
                attachmentsEntities.addAll(dbo.getAttachments());
            }

            if (nonEmpty(dbo.getCopyHistory())) {
                attachmentsEntities.addAll(dbo.getCopyHistory());
            }

            if (nonEmpty(attachmentsEntities)) {
                cv.put(NewsColumns.ATTACHMENTS_JSON, GSON.toJson(AttachmentsEntity.from(attachmentsEntities)));
            } else {
                cv.putNull(NewsColumns.ATTACHMENTS_JSON);
            }
        }

        return cv;
    }

    private static FeedListEntity mapList(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(FeedListsColumns.TITLE));

        FeedListEntity entity = new FeedListEntity(id).setTitle(title);

        String sources = cursor.getString(cursor.getColumnIndexOrThrow(FeedListsColumns.SOURCE_IDS));

        int[] sourceIds = null;

        if (nonEmpty(sources)) {
            String[] ids = sources.split(",");

            sourceIds = new int[ids.length];

            for (int i = 0; i < ids.length; i++) {
                sourceIds[i] = Integer.parseInt(ids[i]);
            }
        }

        return entity.setSourceIds(sourceIds)
                .setNoReposts(cursor.getInt(cursor.getColumnIndexOrThrow(FeedListsColumns.NO_REPOSTS)) == 1);
    }

    @Override
    public Single<List<NewsEntity>> findByCriteria(@NonNull FeedCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getNewsContentUriFor(criteria.getAccountId());
            List<NewsEntity> data = new ArrayList<>();

            synchronized (storeLock) {
                Cursor cursor;
                if (criteria.getRange() != null) {
                    DatabaseIdRange range = criteria.getRange();
                    cursor = getContext().getContentResolver().query(uri, null,
                            BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?",
                            new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())}, null);
                } else {
                    cursor = getContext().getContentResolver().query(uri, null, null, null, null);
                }

                if (nonNull(cursor)) {
                    while (cursor.moveToNext()) {
                        if (e.isDisposed()) {
                            break;
                        }

                        data.add(mapNewsBase(cursor));
                    }

                    cursor.close();
                }
            }

            e.onSuccess(data);
        });
    }

    @Override
    public Single<int[]> store(int accountId, @NonNull List<NewsEntity> dbos, @Nullable OwnerEntities owners, boolean clearBeforeStore) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getNewsContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            if (clearBeforeStore) {
                // for performance test (before - 500-600ms, after - 200-300ms)
                //operations.add(ContentProviderOperation.newDelete(MessengerContentProvider.getNewsAttachmentsContentUriFor(accountId))
                //        .build());

                operations.add(ContentProviderOperation.newDelete(uri).build());
            }

            int[] indexes = new int[dbos.size()];
            for (int i = 0; i < dbos.size(); i++) {
                NewsEntity dbo = dbos.get(i);

                ContentValues cv = getCV(dbo);

                ContentProviderOperation mainPostHeaderOperation = ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build();

                int mainPostHeaderIndex = addToListAndReturnIndex(operations, mainPostHeaderOperation);
                indexes[i] = mainPostHeaderIndex;
            }

            if (nonNull(owners)) {
                OwnersStorage.appendOwnersInsertOperations(operations, accountId, owners);
            }

            ContentProviderResult[] results;

            synchronized (storeLock) {
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

    @Override
    public Completable storeLists(int accountid, @NonNull List<FeedListEntity> entities) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getFeedListsContentUriFor(accountid);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation.newDelete(uri)
                    .build());

            for (FeedListEntity entity : entities) {
                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(FeedListsColumns.getCV(entity))
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }

    @Override
    public Single<List<FeedListEntity>> getAllLists(@NonNull FeedSourceCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFeedListsContentUriFor(criteria.getAccountId());
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            List<FeedListEntity> data = new ArrayList<>(safeCountOf(cursor));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    data.add(mapList(cursor));
                }

                cursor.close();
            }

            e.onSuccess(data);
        });
    }

    /*private void fillAttachmentsOperations(int accountId, @NonNull VKApiAttachment attachment, @NonNull List<ContentProviderOperation> target,
                                           int parentPostHeaderOperationIndex) {
        Logger.d("fillAttachmentsOperations", "attachment: " + attachment.toAttachmentString());

        ContentValues cv = new ContentValues();
        cv.put(NewsAttachmentsColumns.TYPE, Types.from(attachment.getType()));
        cv.put(NewsAttachmentsColumns.DATA, serializeAttachment(attachment));

        target.add(ContentProviderOperation.newInsert(MessengerContentProvider.getNewsAttachmentsContentUriFor(accountId))
                .withValues(cv)
                .withValueBackReference(NewsAttachmentsColumns.N_ID, parentPostHeaderOperationIndex)
                .build());
    }*/

    private NewsEntity mapNewsBase(Cursor cursor) {
        String friendString = cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.TAG_FRIENDS));

        ArrayList<Integer> friends = null;
        if (nonEmpty(friendString)) {
            String[] strArray = friendString.split(",");
            Integer[] intArray = new Integer[strArray.length];
            for (int i = 0; i < strArray.length; i++) {
                intArray[i] = Integer.parseInt(strArray[i]);
            }
            friends = new ArrayList<>(Arrays.asList(intArray));
        }

        NewsEntity dbo = new NewsEntity()
                .setFriendsTags(friends)
                .setType(cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.TYPE)))
                .setSourceId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.SOURCE_ID)))
                .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(NewsColumns.DATE)))
                .setPostId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.POST_ID)))
                .setPostType(cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.POST_TYPE)))
                .setFinalPost(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.FINAL_POST)) == 1)
                .setCopyOwnerId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COPY_OWNER_ID)))
                .setCopyPostId(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COPY_POST_ID)))
                .setCopyPostDate(cursor.getLong(cursor.getColumnIndexOrThrow(NewsColumns.COPY_POST_DATE)))
                .setText(cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.TEXT)))
                .setCanEdit(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_EDIT)) == 1)
                .setCanDelete(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_DELETE)) == 1)
                .setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COMMENT_COUNT)))
                .setCanPostComment(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.COMMENT_CAN_POST)) == 1)
                .setLikesCount(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.LIKE_COUNT)))
                .setUserLikes(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.USER_LIKE)) == 1)
                .setCanLike(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_LIKE)) == 1)
                .setCanPublish(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.CAN_PUBLISH)) == 1)
                .setRepostCount(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.REPOSTS_COUNT)))
                .setUserReposted(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.USER_REPOSTED)) == 1)
                .setViews(cursor.getInt(cursor.getColumnIndexOrThrow(NewsColumns.VIEWS)));

        String attachmentsJson = cursor.getString(cursor.getColumnIndexOrThrow(NewsColumns.ATTACHMENTS_JSON));

        if (nonEmpty(attachmentsJson)) {
            AttachmentsEntity attachmentsEntity = GSON.fromJson(attachmentsJson, AttachmentsEntity.class);
            if (nonNull(attachmentsEntity) && !Utils.isEmpty(attachmentsEntity.getEntities())) {
                List<Entity> all = attachmentsEntity.getEntities();

                List<Entity> attachmentsOnly = new ArrayList<>(all.size());
                List<PostEntity> copiesOnly = new ArrayList<>(0);

                for (Entity a : all) {
                    if (a instanceof PostEntity) {
                        copiesOnly.add((PostEntity) a);
                    } else {
                        attachmentsOnly.add(a);
                    }
                }

                dbo.setAttachments(attachmentsOnly);
                dbo.setCopyHistory(copiesOnly);
            } else {
                dbo.setCopyHistory(null);
                dbo.setAttachments(null);
            }
        } else {
            dbo.setCopyHistory(null);
            dbo.setAttachments(null);
        }

        return dbo;
    }
}