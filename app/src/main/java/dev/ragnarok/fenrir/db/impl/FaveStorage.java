package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.db.impl.OwnersStorage.appendOwnersInsertOperations;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.db.DatabaseIdRange;
import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.FaveArticlesColumns;
import dev.ragnarok.fenrir.db.column.FaveLinksColumns;
import dev.ragnarok.fenrir.db.column.FavePageColumns;
import dev.ragnarok.fenrir.db.column.FavePhotosColumns;
import dev.ragnarok.fenrir.db.column.FavePostsColumns;
import dev.ragnarok.fenrir.db.column.FaveProductColumns;
import dev.ragnarok.fenrir.db.column.FaveVideosColumns;
import dev.ragnarok.fenrir.db.interfaces.IFaveStorage;
import dev.ragnarok.fenrir.db.model.entity.ArticleEntity;
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity;
import dev.ragnarok.fenrir.db.model.entity.FaveLinkEntity;
import dev.ragnarok.fenrir.db.model.entity.FavePageEntity;
import dev.ragnarok.fenrir.db.model.entity.MarketEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.db.model.entity.UserEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoEntity;
import dev.ragnarok.fenrir.model.criteria.FaveArticlesCriteria;
import dev.ragnarok.fenrir.model.criteria.FavePhotosCriteria;
import dev.ragnarok.fenrir.model.criteria.FavePostsCriteria;
import dev.ragnarok.fenrir.model.criteria.FaveProductsCriteria;
import dev.ragnarok.fenrir.model.criteria.FaveVideosCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

class FaveStorage extends AbsStorage implements IFaveStorage {

    FaveStorage(@NonNull AppStorages mRepositoryContext) {
        super(mRepositoryContext);
    }

    private static ContentValues createFaveCv(FavePageEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(BaseColumns._ID, dbo.getId());
        cv.put(FavePageColumns.DESCRIPTION, dbo.getDescription());
        cv.put(FavePageColumns.FAVE_TYPE, dbo.getFaveType());
        cv.put(FavePageColumns.UPDATED_TIME, dbo.getUpdateDate());
        return cv;
    }

    private static UserEntity mapUser(int accountId, int id) {
        return Injection.provideStores().owners().findUserDboById(accountId, id).blockingGet().get();
    }

    private static CommunityEntity mapGroup(int accountId, int id) {
        return Injection.provideStores().owners().findCommunityDboById(accountId, Math.abs(id)).blockingGet().get();
    }

    private static FavePageEntity mapFaveUserDbo(Cursor cursor, int accountId) {
        return new FavePageEntity(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                .setDescription(cursor.getString(cursor.getColumnIndex(FavePageColumns.DESCRIPTION)))
                .setUpdateDate(cursor.getLong(cursor.getColumnIndex(FavePageColumns.UPDATED_TIME)))
                .setFaveType(cursor.getString(cursor.getColumnIndex(FavePageColumns.FAVE_TYPE)))
                .setUser(mapUser(accountId, cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))));
    }

    private static FavePageEntity mapFaveGroupDbo(Cursor cursor, int accountId) {
        return new FavePageEntity(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                .setDescription(cursor.getString(cursor.getColumnIndex(FavePageColumns.DESCRIPTION)))
                .setUpdateDate(cursor.getLong(cursor.getColumnIndex(FavePageColumns.UPDATED_TIME)))
                .setFaveType(cursor.getString(cursor.getColumnIndex(FavePageColumns.FAVE_TYPE)))
                .setGroup(mapGroup(accountId, cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))));
    }

    private static PhotoEntity mapFavePhoto(Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(FavePhotosColumns.PHOTO));
        return GSON.fromJson(json, PhotoEntity.class);
    }

    private static PhotoEntity mapFaveLinkPhoto(Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(FaveLinksColumns.PHOTO));
        return GSON.fromJson(json, PhotoEntity.class);
    }

    @Override
    public Single<List<PostEntity>> getFavePosts(@NonNull FavePostsCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFavePostsContentUriFor(criteria.getAccountId());
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

            List<PostEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapFavePosts(cursor));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
        });
    }

    @Override
    public Completable storePosts(int accountId, List<PostEntity> posts, OwnerEntities owners, boolean clearBeforeStore) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getFavePostsContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearBeforeStore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            for (PostEntity dbo : posts) {
                ContentValues cv = new ContentValues();
                cv.put(FavePostsColumns.POST, GSON.toJson(dbo));

                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            if (nonNull(owners)) {
                appendOwnersInsertOperations(operations, accountId, owners);
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }

    @Override
    public Single<List<FaveLinkEntity>> getFaveLinks(int accountId) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveLinksContentUriFor(accountId);
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            List<FaveLinkEntity> data = new ArrayList<>();
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    data.add(mapFaveLink(cursor));
                }

                cursor.close();
            }

            e.onSuccess(data);
        });
    }

    @Override
    public Completable removeLink(int accountId, String id) {
        return Completable.fromAction(() -> {
            Uri uri = MessengerContentProvider.getFaveLinksContentUriFor(accountId);
            String where = FaveLinksColumns.LINK_ID + " LIKE ?";
            String[] args = {id};
            getContentResolver().delete(uri, where, args);
        });
    }

    @Override
    public Completable storeLinks(int accountId, List<FaveLinkEntity> entities, boolean clearBefore) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getFaveLinksContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            if (clearBefore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            for (FaveLinkEntity entity : entities) {
                ContentValues cv = new ContentValues();
                cv.put(FaveLinksColumns.LINK_ID, entity.getId());
                cv.put(FaveLinksColumns.URL, entity.getUrl());
                cv.put(FaveLinksColumns.TITLE, entity.getTitle());
                cv.put(FaveLinksColumns.DESCRIPTION, entity.getDescription());
                cv.put(FaveLinksColumns.PHOTO, GSON.toJson(entity.getPhoto()));

                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Completable removePage(int accountId, int ownerId, boolean isUser) {
        return Completable.fromAction(() -> {
            Uri uri = isUser ? MessengerContentProvider.getFaveUsersContentUriFor(accountId) : MessengerContentProvider.getFaveGroupsContentUriFor(accountId);
            final String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(ownerId)};
            getContentResolver().delete(uri, where, args);
        });
    }

    @Override
    public Single<List<FavePageEntity>> getFaveUsers(int accountId) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveUsersContentUriFor(accountId);

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            List<FavePageEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapFaveUserDbo(cursor, accountId));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
        });
    }

    @Override
    public Single<List<FavePageEntity>> getFaveGroups(int accountId) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveGroupsContentUriFor(accountId);

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            List<FavePageEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapFaveGroupDbo(cursor, accountId));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
        });
    }

    @Override
    public Single<int[]> storePhotos(int accountId, List<PhotoEntity> photos, boolean clearBeforeStore) {
        return Single.create(e -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            Uri uri = MessengerContentProvider.getFavePhotosContentUriFor(accountId);

            if (clearBeforeStore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            // массив для хранения индексов операций вставки для каждого фото
            int[] indexes = new int[photos.size()];

            for (int i = 0; i < photos.size(); i++) {
                PhotoEntity dbo = photos.get(i);

                ContentValues cv = new ContentValues();
                cv.put(FavePhotosColumns.PHOTO_ID, dbo.getId());
                cv.put(FavePhotosColumns.OWNER_ID, dbo.getOwnerId());
                cv.put(FavePhotosColumns.POST_ID, dbo.getPostId());
                cv.put(FavePhotosColumns.PHOTO, GSON.toJson(dbo));

                int index = addToListAndReturnIndex(operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());

                indexes[i] = index;
            }

            ContentProviderResult[] results = getContentResolver()
                    .applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int[] ids = new int[results.length];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];

                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            e.onSuccess(ids);
        });
    }

    @Override
    public Single<List<PhotoEntity>> getPhotos(FavePhotosCriteria criteria) {
        return Single.create(e -> {
            String where;
            String[] args;

            Uri uri = MessengerContentProvider.getFavePhotosContentUriFor(criteria.getAccountId());
            DatabaseIdRange range = criteria.getRange();

            if (isNull(range)) {
                where = null;
                args = null;
            } else {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?";
                args = new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())};
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            List<PhotoEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapFavePhoto(cursor));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
        });
    }

    @Override
    public Single<List<VideoEntity>> getVideos(FaveVideosCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveVideosContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            DatabaseIdRange range = criteria.getRange();
            if (nonNull(range)) {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?";
                args = new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())};
            } else {
                where = null;
                args = null;
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            List<VideoEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapVideo(cursor));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
        });
    }

    private VideoEntity mapVideo(Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(FaveVideosColumns.VIDEO));
        return GSON.fromJson(json, VideoEntity.class);
    }

    @Override
    public Single<List<ArticleEntity>> getArticles(FaveArticlesCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveArticlesContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            DatabaseIdRange range = criteria.getRange();
            if (nonNull(range)) {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?";
                args = new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())};
            } else {
                where = null;
                args = null;
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            List<ArticleEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapArticle(cursor));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
        });
    }

    @Override
    public Single<List<MarketEntity>> getProducts(FaveProductsCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveProductsContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            DatabaseIdRange range = criteria.getRange();
            if (nonNull(range)) {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?";
                args = new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())};
            } else {
                where = null;
                args = null;
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            List<MarketEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapProduct(cursor));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
        });
    }

    private ArticleEntity mapArticle(Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(FaveArticlesColumns.ARTICLE));
        return GSON.fromJson(json, ArticleEntity.class);
    }

    private MarketEntity mapProduct(Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(FaveProductColumns.PRODUCT));
        return GSON.fromJson(json, MarketEntity.class);
    }

    @Override
    public Single<int[]> storeVideos(int accountId, List<VideoEntity> videos, boolean clearBeforeStore) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveVideosContentUriFor(accountId);
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearBeforeStore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            int[] indexes = new int[videos.size()];

            for (int i = 0; i < videos.size(); i++) {
                VideoEntity dbo = videos.get(i);
                ContentValues cv = new ContentValues();
                cv.put(FaveVideosColumns.VIDEO, GSON.toJson(dbo));

                int index = addToListAndReturnIndex(operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
                indexes[i] = index;
            }

            ContentProviderResult[] results = getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int[] ids = new int[results.length];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];

                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            e.onSuccess(ids);
        });
    }

    @Override
    public Single<int[]> storeArticles(int accountId, List<ArticleEntity> articles, boolean clearBeforeStore) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveArticlesContentUriFor(accountId);
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearBeforeStore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            int[] indexes = new int[articles.size()];

            for (int i = 0; i < articles.size(); i++) {
                ArticleEntity dbo = articles.get(i);
                ContentValues cv = new ContentValues();
                cv.put(FaveArticlesColumns.ARTICLE, GSON.toJson(dbo));

                int index = addToListAndReturnIndex(operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
                indexes[i] = index;
            }

            ContentProviderResult[] results = getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int[] ids = new int[results.length];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];

                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            e.onSuccess(ids);
        });
    }

    @Override
    public Single<int[]> storeProducts(int accountId, List<MarketEntity> products, boolean clearBeforeStore) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFaveProductsContentUriFor(accountId);
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearBeforeStore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            int[] indexes = new int[products.size()];

            for (int i = 0; i < products.size(); i++) {
                MarketEntity dbo = products.get(i);
                ContentValues cv = new ContentValues();
                cv.put(FaveProductColumns.PRODUCT, GSON.toJson(dbo));

                int index = addToListAndReturnIndex(operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
                indexes[i] = index;
            }

            ContentProviderResult[] results = getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int[] ids = new int[results.length];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];

                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            e.onSuccess(ids);
        });
    }

    @Override
    public Completable storePages(int accountId, List<FavePageEntity> users, boolean clearBeforeStore) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getFaveUsersContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            if (clearBeforeStore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            for (int i = 0; i < users.size(); i++) {
                FavePageEntity dbo = users.get(i);
                ContentValues cv = createFaveCv(dbo);

                addToListAndReturnIndex(operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            if (!operations.isEmpty()) {
                getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            }

            e.onComplete();
        });
    }

    @Override
    public Completable storeGroups(int accountId, List<FavePageEntity> groups, boolean clearBeforeStore) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getFaveGroupsContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            if (clearBeforeStore) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .build());
            }

            for (int i = 0; i < groups.size(); i++) {
                FavePageEntity dbo = groups.get(i);
                ContentValues cv = createFaveCv(dbo);

                addToListAndReturnIndex(operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            if (!operations.isEmpty()) {
                getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            }

            e.onComplete();
        });
    }

    private FaveLinkEntity mapFaveLink(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(FaveLinksColumns.LINK_ID));
        String url = cursor.getString(cursor.getColumnIndex(FaveLinksColumns.URL));
        return new FaveLinkEntity(id, url)
                .setTitle(cursor.getString(cursor.getColumnIndex(FaveLinksColumns.TITLE)))
                .setDescription(cursor.getString(cursor.getColumnIndex(FaveLinksColumns.DESCRIPTION)))
                .setPhoto(mapFaveLinkPhoto(cursor));
    }

    private PostEntity mapFavePosts(Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(FavePostsColumns.POST));
        return GSON.fromJson(json, PostEntity.class);
    }
}
