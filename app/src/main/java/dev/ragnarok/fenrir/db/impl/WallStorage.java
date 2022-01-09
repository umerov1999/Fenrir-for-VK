package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.db.MessengerContentProvider.getPostsContentUriFor;
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
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.PostsColumns;
import dev.ragnarok.fenrir.db.interfaces.Cancelable;
import dev.ragnarok.fenrir.db.interfaces.IWallStorage;
import dev.ragnarok.fenrir.db.model.PostPatch;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

class WallStorage extends AbsStorage implements IWallStorage {

    /**
     * Идентификатор для сохранения "черновиков постов"
     */
    private static final int DRAFT_POST_ID = -1;
    /**
     * Идентификатор для сохранения временных постов, репостов, шаринга и прочего
     */
    private static final int TEMP_POST_ID = -2;

    WallStorage(@NonNull AppStorages base) {
        super(base);
    }

    private static void appendDboAttachmentsAndCopies(PostEntity dbo, List<ContentProviderOperation> operations,
                                                      int accountId, int mainPostHeaderIndex) {
        if (nonEmpty(dbo.getAttachments())) {
            for (Entity attachmentEntity : dbo.getAttachments()) {
                appendAttachOperationWithBackReference(operations, accountId, AttachToType.POST, mainPostHeaderIndex, attachmentEntity);
            }
        }
        if (nonEmpty(dbo.getCopyHierarchy())) {
            for (PostEntity copyDbo : dbo.getCopyHierarchy()) {
                appendAttachOperationWithBackReference(operations, accountId, AttachToType.POST, mainPostHeaderIndex, copyDbo);
            }
        }
    }

    private static ContentValues createCv(PostEntity dbo) {
        ContentValues cv = new ContentValues();

        cv.put(PostsColumns.POST_ID, dbo.getId());
        cv.put(PostsColumns.OWNER_ID, dbo.getOwnerId());
        cv.put(PostsColumns.FROM_ID, dbo.getFromId());
        cv.put(PostsColumns.DATE, dbo.getDate());
        cv.put(PostsColumns.TEXT, dbo.getText());
        cv.put(PostsColumns.REPLY_OWNER_ID, dbo.getReplyOwnerId());
        cv.put(PostsColumns.REPLY_POST_ID, dbo.getReplyPostId());
        cv.put(PostsColumns.FRIENDS_ONLY, dbo.isFriendsOnly());
        cv.put(PostsColumns.COMMENTS_COUNT, dbo.getCommentsCount());
        cv.put(PostsColumns.CAN_POST_COMMENT, dbo.isCanPostComment());
        cv.put(PostsColumns.LIKES_COUNT, dbo.getLikesCount());
        cv.put(PostsColumns.USER_LIKES, dbo.isUserLikes());
        cv.put(PostsColumns.CAN_LIKE, dbo.isCanLike());
        cv.put(PostsColumns.CAN_PUBLISH, dbo.isCanPublish());
        cv.put(PostsColumns.CAN_EDIT, dbo.isCanEdit());
        cv.put(PostsColumns.REPOSTS_COUNT, dbo.getRepostCount());
        cv.put(PostsColumns.USER_REPOSTED, dbo.isUserReposted());
        cv.put(PostsColumns.POST_TYPE, dbo.getPostType());
        cv.put(PostsColumns.SIGNED_ID, dbo.getSignedId());
        cv.put(PostsColumns.CREATED_BY, dbo.getCreatedBy());
        cv.put(PostsColumns.CAN_PIN, dbo.isCanPin());
        cv.put(PostsColumns.IS_PINNED, dbo.isPinned());
        cv.put(PostsColumns.DELETED, dbo.isDeleted());

        int attachmentsCount = nonNull(dbo.getAttachments()) ? safeCountOf(dbo.getAttachments()) : 0;
        int copiesCount = safeCountOf(dbo.getCopyHierarchy());

        cv.put(PostsColumns.ATTACHMENTS_MASK, attachmentsCount + copiesCount);

        if (nonNull(dbo.getSource())) {
            cv.put(PostsColumns.POST_SOURCE, GSON.toJson(dbo.getSource()));
        } else {
            cv.putNull(PostsColumns.POST_SOURCE);
        }

        cv.put(PostsColumns.VIEWS, dbo.getViews());
        return cv;
    }

    private static int getVkPostIdForEditingType(@EditingPostType int type) {
        switch (type) {
            case EditingPostType.DRAFT:
                return DRAFT_POST_ID;
            case EditingPostType.TEMP:
                return TEMP_POST_ID;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Single<int[]> storeWallEntities(int accountId, @NonNull List<PostEntity> dbos,
                                           @Nullable OwnerEntities owners, @Nullable IClearWallTask clearWall) {
        return Single.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (nonNull(clearWall)) {
                operations.add(operationForClearWall(accountId, clearWall.getOwnerId()));
            }

            int[] indexes = new int[dbos.size()];

            for (int i = 0; i < dbos.size(); i++) {
                PostEntity dbo = dbos.get(i);

                ContentValues cv = createCv(dbo);

                ContentProviderOperation mainPostHeaderOperation = ContentProviderOperation
                        .newInsert(getPostsContentUriFor(accountId))
                        .withValues(cv)
                        .build();

                int mainPostHeaderIndex = addToListAndReturnIndex(operations, mainPostHeaderOperation);
                indexes[i] = mainPostHeaderIndex;

                appendDboAttachmentsAndCopies(dbo, operations, accountId, mainPostHeaderIndex);
            }

            if (nonNull(owners)) {
                OwnersStorage.appendOwnersInsertOperations(operations, accountId, owners);
            }

            ContentProviderResult[] results = getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

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
    public Single<Integer> replacePost(int accountId, @NonNull PostEntity dbo) {
        return Single.create(e -> {
            Uri uri = getPostsContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            ContentValues cv = createCv(dbo);

            if (dbo.getDbid() > 0) {
                cv.put(BaseColumns._ID, dbo.getDbid());

                // если пост был сохранен ранее - удаляем старые данные
                // и сохраняем заново с тем же _ID
                operations.add(ContentProviderOperation.newDelete(uri)
                        .withSelection(BaseColumns._ID + " = ?", new String[]{String.valueOf(dbo.getDbid())})
                        .build());
            }

            ContentProviderOperation main = ContentProviderOperation.newInsert(uri)
                    .withValues(cv)
                    .build();

            int mainPostIndex = addToListAndReturnIndex(operations, main);

            appendDboAttachmentsAndCopies(dbo, operations, accountId, mainPostIndex);

            ContentProviderResult[] results = getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int dbid = extractId(results[mainPostIndex]);
            e.onSuccess(dbid);
        });
    }

    private Single<Integer> insertNew(int accountId, int vkId, int ownerId, int authorId) {
        return Single.fromCallable(() -> {
            Uri uri = getPostsContentUriFor(accountId);

            ContentValues cv = new ContentValues();

            cv.put(PostsColumns.POST_ID, vkId);
            cv.put(PostsColumns.OWNER_ID, ownerId);
            cv.put(PostsColumns.FROM_ID, authorId);

            Uri resultUri = getContentResolver().insert(uri, cv);
            return Integer.parseInt(resultUri.getLastPathSegment());
        });
    }

    @Override
    public Single<PostEntity> getEditingPost(int accountId, int ownerId, @EditingPostType int type, boolean includeAttachment) {
        int vkPostId = getVkPostIdForEditingType(type);

        return findPostById(accountId, ownerId, vkPostId, includeAttachment)
                .flatMap(optional -> {
                    if (optional.nonEmpty()) {
                        return Single.just(optional.get());
                    }

                    return insertNew(accountId, vkPostId, ownerId, accountId)
                            .flatMap(dbid -> findPostById(accountId, ownerId, vkPostId, includeAttachment)
                                    .map(Optional::get));
                });
    }

    @Override
    public Completable deletePost(int accountId, int dbid) {
        return Completable.create(e -> {
            getContentResolver().delete(getPostsContentUriFor(accountId),
                    BaseColumns._ID + " = ?", new String[]{String.valueOf(dbid)});
            e.onComplete();
        });
    }

    @Override
    public Single<Optional<PostEntity>> findPostById(int accountId, int dbid) {
        return Single.create(e -> {
            Cancelable cancelable = e::isDisposed;

            Uri uri = getPostsContentUriFor(accountId);
            final String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(dbid)};
            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            PostEntity dbo = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    dbo = mapDbo(accountId, cursor, true, true, cancelable);
                }

                cursor.close();
            }

            e.onSuccess(Optional.wrap(dbo));
        });
    }

    @Override
    public Single<Optional<PostEntity>> findPostById(int accountId, int ownerId, int vkpostId, boolean includeAttachment) {
        return Single.create(e -> {
            Uri uri = getPostsContentUriFor(accountId);

            Cursor cursor = getContentResolver().query(uri, null,
                    PostsColumns.OWNER_ID + " = ? AND " + PostsColumns.POST_ID + " = ?",
                    new String[]{String.valueOf(ownerId), String.valueOf(vkpostId)}, null);

            PostEntity dbo = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    dbo = mapDbo(accountId, cursor, includeAttachment, includeAttachment, e::isDisposed);
                }

                cursor.close();
            }

            e.onSuccess(Optional.wrap(dbo));
        });
    }

    @Override
    public Single<List<PostEntity>> findDbosByCriteria(@NonNull WallCriteria criteria) {
        return Single.create(emitter -> {
            int accountId = criteria.getAccountId();

            Cursor cursor = buildCursor(criteria);
            Cancelable cancelable = emitter::isDisposed;
            List<PostEntity> dbos = new ArrayList<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapDbo(accountId, cursor, true, false, cancelable));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Completable update(int accountId, int ownerId, int postId, @NonNull PostPatch update) {
        return Completable.create(e -> {
            ContentValues cv = new ContentValues();

            if (nonNull(update.getDeletePatch())) {
                cv.put(PostsColumns.DELETED, update.getDeletePatch().isDeleted());
            }

            if (nonNull(update.getPinPatch())) {
                cv.put(PostsColumns.IS_PINNED, update.getPinPatch().isPinned());
            }

            if (nonNull(update.getLikePatch())) {
                cv.put(PostsColumns.LIKES_COUNT, update.getLikePatch().getCount());
                cv.put(PostsColumns.USER_LIKES, update.getLikePatch().isLiked());
            }

            Uri uri = getPostsContentUriFor(accountId);

            getContentResolver().update(uri, cv,
                    PostsColumns.POST_ID + " = ? AND " + PostsColumns.OWNER_ID + " = ?",
                    new String[]{String.valueOf(postId), String.valueOf(ownerId)});

            e.onComplete();
        });
    }

    @Override
    public Completable invalidatePost(int accountId, int postVkid, int postOwnerId) {
        return Completable.fromAction(() -> {
            Uri uri = getPostsContentUriFor(accountId);
            String where = PostsColumns.POST_ID + " = ? AND " + PostsColumns.OWNER_ID + " = ?";
            String[] args = {String.valueOf(postVkid), String.valueOf(postOwnerId)};

            getContentResolver().delete(uri, where, args);
        });
    }

    private Cursor buildCursor(WallCriteria criteria) {
        // не грузить посты, которые находятся в редактировании
        // или являються копией других постов
        String where = PostsColumns.POST_ID + " != " + DRAFT_POST_ID +
                " AND " + PostsColumns.POST_ID + " != " + TEMP_POST_ID +
                " AND " + PostsColumns.OWNER_ID + " = " + criteria.getOwnerId();

        if (criteria.getRange() != null) {
            where = where +
                    " AND " + BaseColumns._ID + " <= " + criteria.getRange().getLast() +
                    " AND " + BaseColumns._ID + " >= " + criteria.getRange().getFirst();
        }

        switch (criteria.getMode()) {
            case WallCriteria.MODE_ALL:
                // Загружаем все посты, кроме отложенных и предлагаемых
                where = where + " AND " + PostsColumns.POST_TYPE + " NOT IN (" + VKApiPost.Type.POSTPONE + ", " + VKApiPost.Type.SUGGEST + ") ";
                break;

            case WallCriteria.MODE_OWNER:
                where = where +
                        " AND " + PostsColumns.FROM_ID + " = " + criteria.getOwnerId() +
                        " AND " + PostsColumns.POST_TYPE + " NOT IN (" + VKApiPost.Type.POSTPONE + ", " + VKApiPost.Type.SUGGEST + ") ";
                break;
            case WallCriteria.MODE_SCHEDULED:
                where = where + " AND " + PostsColumns.POST_TYPE + " = " + VKApiPost.Type.POSTPONE;
                break;
            case WallCriteria.MODE_SUGGEST:
                where = where + " AND " + PostsColumns.POST_TYPE + " = " + VKApiPost.Type.SUGGEST;
                break;
        }

        return getContentResolver().query(
                getPostsContentUriFor(criteria.getAccountId()), null, where, null,
                PostsColumns.IS_PINNED + " DESC, " + PostsColumns.POST_ID + " DESC");
    }

    private PostEntity mapDbo(int accountId, Cursor cursor, boolean includeAttachments, boolean forceAttachments, @NonNull Cancelable cancelable) {
        int dbid = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        int attachmentsMask = cursor.getInt(cursor.getColumnIndex(PostsColumns.ATTACHMENTS_MASK));
        int postId = cursor.getInt(cursor.getColumnIndex(PostsColumns.POST_ID));
        int ownerId = cursor.getInt(cursor.getColumnIndex(PostsColumns.OWNER_ID));

        PostEntity dbo = new PostEntity().set(postId, ownerId)
                .setDbid(dbid)
                .setFromId(cursor.getInt(cursor.getColumnIndex(PostsColumns.FROM_ID)))
                .setDate(cursor.getLong(cursor.getColumnIndex(PostsColumns.DATE)))
                .setText(cursor.getString(cursor.getColumnIndex(PostsColumns.TEXT)))
                .setReplyOwnerId(cursor.getInt(cursor.getColumnIndex(PostsColumns.REPLY_OWNER_ID)))
                .setReplyPostId(cursor.getInt(cursor.getColumnIndex(PostsColumns.REPLY_POST_ID)))
                .setFriendsOnly(cursor.getInt(cursor.getColumnIndex(PostsColumns.FRIENDS_ONLY)) == 1)
                .setCommentsCount(cursor.getInt(cursor.getColumnIndex(PostsColumns.COMMENTS_COUNT)))
                .setCanPostComment(cursor.getInt(cursor.getColumnIndex(PostsColumns.CAN_POST_COMMENT)) == 1)
                .setLikesCount(cursor.getInt(cursor.getColumnIndex(PostsColumns.LIKES_COUNT)))
                .setCanLike(cursor.getInt(cursor.getColumnIndex(PostsColumns.CAN_LIKE)) == 1)
                .setUserLikes(cursor.getInt(cursor.getColumnIndex(PostsColumns.USER_LIKES)) == 1)
                .setRepostCount(cursor.getInt(cursor.getColumnIndex(PostsColumns.REPOSTS_COUNT)))
                .setCanPublish(cursor.getInt(cursor.getColumnIndex(PostsColumns.CAN_PUBLISH)) == 1)
                .setUserReposted(cursor.getInt(cursor.getColumnIndex(PostsColumns.USER_REPOSTED)) == 1)
                .setPostType(cursor.getInt(cursor.getColumnIndex(PostsColumns.POST_TYPE)))
                .setSignedId(cursor.getInt(cursor.getColumnIndex(PostsColumns.SIGNED_ID)))
                .setCreatedBy(cursor.getInt(cursor.getColumnIndex(PostsColumns.CREATED_BY)))
                .setCanPin(cursor.getInt(cursor.getColumnIndex(PostsColumns.CAN_PIN)) == 1)
                .setPinned(cursor.getInt(cursor.getColumnIndex(PostsColumns.IS_PINNED)) == 1)
                .setDeleted(cursor.getInt(cursor.getColumnIndex(PostsColumns.DELETED)) == 1)
                .setViews(cursor.getInt(cursor.getColumnIndex(PostsColumns.VIEWS)))
                .setCanEdit(cursor.getInt(cursor.getColumnIndex(PostsColumns.CAN_EDIT)) == 1);

        String postSourceText = cursor.getString(cursor.getColumnIndex(PostsColumns.POST_SOURCE));
        if (nonEmpty(postSourceText)) {
            dbo.setSource(GSON.fromJson(postSourceText, PostEntity.SourceDbo.class));
        }

        List<PostEntity> copiesDbos = new ArrayList<>(0);

        if (includeAttachments && (attachmentsMask > 0 || forceAttachments)) {
            List<Entity> attachments = getStores()
                    .attachments()
                    .getAttachmentsDbosSync(accountId, AttachToType.POST, dbid, cancelable);

            // Так как история репостов хранится вместе с вложениями,
            // в этом месте пересохраняем эту историю в другой список
            Iterator<Entity> iterator = attachments.iterator();
            while (iterator.hasNext()) {
                Entity next = iterator.next();

                if (next instanceof PostEntity) {
                    copiesDbos.add((PostEntity) next);
                    iterator.remove();
                }
            }

            dbo.setAttachments(attachments);
        } else {
            dbo.setAttachments(null);
        }

        dbo.setCopyHierarchy(copiesDbos);
        return dbo;
    }

    private ContentProviderOperation operationForClearWall(int accountId, int ownerId) {
        String where = PostsColumns.OWNER_ID + " = ? " +
                " AND " + PostsColumns.POST_ID + " != ? " +
                " AND " + PostsColumns.POST_ID + " != ?";

        String[] args = {String.valueOf(ownerId), String.valueOf(DRAFT_POST_ID), String.valueOf(TEMP_POST_ID)};

        Uri uri = getPostsContentUriFor(accountId);

        return ContentProviderOperation
                .newDelete(uri)
                .withSelection(where, args)
                .build();
    }
}