package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.Includes.stores
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFaveArticlesContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFaveGroupsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFaveLinksContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFavePhotosContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFavePostsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFaveProductsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFaveUsersContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getFaveVideosContentUriFor
import dev.ragnarok.fenrir.db.column.*
import dev.ragnarok.fenrir.db.interfaces.IFaveStorage
import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.model.criteria.*
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import kotlin.math.abs

internal class FaveStorage(mRepositoryContext: AppStorages) : AbsStorage(mRepositoryContext),
    IFaveStorage {
    override fun getFavePosts(criteria: FavePostsCriteria): Single<List<PostEntity>> {
        return Single.create { e: SingleEmitter<List<PostEntity>> ->
            val uri = getFavePostsContentUriFor(criteria.accountId)
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val dbos: MutableList<PostEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapFavePosts(cursor))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
        }
    }

    override fun storePosts(
        accountId: Int,
        posts: List<PostEntity>,
        owners: OwnerEntities?,
        clearBeforeStore: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val uri = getFavePostsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeStore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            for (dbo in posts) {
                val cv = ContentValues()
                cv.put(FavePostsColumns.POST, GSON.toJson(dbo))
                operations.add(
                    ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            if (owners != null) {
                OwnersStorage.appendOwnersInsertOperations(operations, accountId, owners)
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            e.onComplete()
        }
    }

    override fun getFaveLinks(accountId: Int): Single<List<FaveLinkEntity>> {
        return Single.create { e: SingleEmitter<List<FaveLinkEntity>> ->
            val uri = getFaveLinksContentUriFor(accountId)
            val cursor = contentResolver.query(uri, null, null, null, null)
            val data: MutableList<FaveLinkEntity> = ArrayList()
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    data.add(mapFaveLink(cursor))
                }
                cursor.close()
            }
            e.onSuccess(data)
        }
    }

    override fun removeLink(accountId: Int, id: String?): Completable {
        return Completable.fromAction {
            val uri = getFaveLinksContentUriFor(accountId)
            val where = FaveLinksColumns.LINK_ID + " LIKE ?"
            val args = arrayOf(id)
            contentResolver.delete(uri, where, args)
        }
    }

    override fun storeLinks(
        accountId: Int,
        entities: List<FaveLinkEntity>,
        clearBefore: Boolean
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val uri = getFaveLinksContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBefore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            for (entity in entities) {
                val cv = ContentValues()
                cv.put(FaveLinksColumns.LINK_ID, entity.id)
                cv.put(FaveLinksColumns.URL, entity.url)
                cv.put(FaveLinksColumns.TITLE, entity.title)
                cv.put(FaveLinksColumns.DESCRIPTION, entity.description)
                cv.put(FaveLinksColumns.PHOTO, GSON.toJson(entity.photo))
                operations.add(
                    ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun removePage(accountId: Int, ownerId: Int, isUser: Boolean): Completable {
        return Completable.fromAction {
            val uri =
                if (isUser) getFaveUsersContentUriFor(accountId) else getFaveGroupsContentUriFor(
                    accountId
                )
            val where = BaseColumns._ID + " = ?"
            val args = arrayOf(ownerId.toString())
            contentResolver.delete(uri, where, args)
        }
    }

    override fun getFaveUsers(accountId: Int): Single<List<FavePageEntity>> {
        return Single.create { e: SingleEmitter<List<FavePageEntity>> ->
            val uri = getFaveUsersContentUriFor(accountId)
            val cursor = contentResolver.query(uri, null, null, null, null)
            val dbos: MutableList<FavePageEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapFaveUserDbo(cursor, accountId))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
        }
    }

    override fun getFaveGroups(accountId: Int): Single<List<FavePageEntity>> {
        return Single.create { e: SingleEmitter<List<FavePageEntity>> ->
            val uri = getFaveGroupsContentUriFor(accountId)
            val cursor = contentResolver.query(uri, null, null, null, null)
            val dbos: MutableList<FavePageEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapFaveGroupDbo(cursor, accountId))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
        }
    }

    override fun storePhotos(
        accountId: Int,
        photos: List<PhotoEntity>,
        clearBeforeStore: Boolean
    ): Single<IntArray> {
        return Single.create { e: SingleEmitter<IntArray> ->
            val operations = ArrayList<ContentProviderOperation>()
            val uri = getFavePhotosContentUriFor(accountId)
            if (clearBeforeStore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }

            // массив для хранения индексов операций вставки для каждого фото
            val indexes = IntArray(photos.size)
            for (i in photos.indices) {
                val dbo = photos[i]
                val cv = ContentValues()
                cv.put(FavePhotosColumns.PHOTO_ID, dbo.id)
                cv.put(FavePhotosColumns.OWNER_ID, dbo.ownerId)
                cv.put(FavePhotosColumns.POST_ID, dbo.postId)
                cv.put(FavePhotosColumns.PHOTO, GSON.toJson(dbo))
                val index = addToListAndReturnIndex(
                    operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
                indexes[i] = index
            }
            val results = contentResolver
                .applyBatch(MessengerContentProvider.AUTHORITY, operations)
            val ids = IntArray(results.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            e.onSuccess(ids)
        }
    }

    override fun getPhotos(criteria: FavePhotosCriteria): Single<List<PhotoEntity>> {
        return Single.create { e: SingleEmitter<List<PhotoEntity>> ->
            val where: String?
            val args: Array<String>?
            val uri = getFavePhotosContentUriFor(criteria.accountId)
            val range = criteria.range
            if (range == null) {
                where = null
                args = null
            } else {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?"
                args = arrayOf(range.first.toString(), range.last.toString())
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val dbos: MutableList<PhotoEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapFavePhoto(cursor))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
        }
    }

    override fun getVideos(criteria: FaveVideosCriteria): Single<List<VideoEntity>> {
        return Single.create { e: SingleEmitter<List<VideoEntity>> ->
            val uri = getFaveVideosContentUriFor(criteria.accountId)
            val where: String?
            val args: Array<String>?
            val range = criteria.range
            if (range != null) {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?"
                args = arrayOf(range.first.toString(), range.last.toString())
            } else {
                where = null
                args = null
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val dbos: MutableList<VideoEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapVideo(cursor))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
        }
    }

    private fun mapVideo(cursor: Cursor): VideoEntity {
        val json = cursor.getString(cursor.getColumnIndexOrThrow(FaveVideosColumns.VIDEO))
        return GSON.fromJson(json, VideoEntity::class.java)
    }

    override fun getArticles(criteria: FaveArticlesCriteria): Single<List<ArticleEntity>> {
        return Single.create { e: SingleEmitter<List<ArticleEntity>> ->
            val uri = getFaveArticlesContentUriFor(criteria.accountId)
            val where: String?
            val args: Array<String>?
            val range = criteria.range
            if (range != null) {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?"
                args = arrayOf(range.first.toString(), range.last.toString())
            } else {
                where = null
                args = null
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val dbos: MutableList<ArticleEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapArticle(cursor))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
        }
    }

    override fun getProducts(criteria: FaveProductsCriteria): Single<List<MarketEntity>> {
        return Single.create { e: SingleEmitter<List<MarketEntity>> ->
            val uri = getFaveProductsContentUriFor(criteria.accountId)
            val where: String?
            val args: Array<String>?
            val range = criteria.range
            if (range != null) {
                where = BaseColumns._ID + " >= ? AND " + BaseColumns._ID + " <= ?"
                args = arrayOf(range.first.toString(), range.last.toString())
            } else {
                where = null
                args = null
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val dbos: MutableList<MarketEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapProduct(cursor))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
        }
    }

    private fun mapArticle(cursor: Cursor): ArticleEntity {
        val json = cursor.getString(cursor.getColumnIndexOrThrow(FaveArticlesColumns.ARTICLE))
        return GSON.fromJson(json, ArticleEntity::class.java)
    }

    private fun mapProduct(cursor: Cursor): MarketEntity {
        val json = cursor.getString(cursor.getColumnIndexOrThrow(FaveProductColumns.PRODUCT))
        return GSON.fromJson(json, MarketEntity::class.java)
    }

    override fun storeVideos(
        accountId: Int,
        videos: List<VideoEntity>,
        clearBeforeStore: Boolean
    ): Single<IntArray> {
        return Single.create { e: SingleEmitter<IntArray> ->
            val uri = getFaveVideosContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeStore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            val indexes = IntArray(videos.size)
            for (i in videos.indices) {
                val dbo = videos[i]
                val cv = ContentValues()
                cv.put(FaveVideosColumns.VIDEO, GSON.toJson(dbo))
                val index = addToListAndReturnIndex(
                    operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
                indexes[i] = index
            }
            val results = contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            val ids = IntArray(results.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            e.onSuccess(ids)
        }
    }

    override fun storeArticles(
        accountId: Int,
        articles: List<ArticleEntity>,
        clearBeforeStore: Boolean
    ): Single<IntArray> {
        return Single.create { e: SingleEmitter<IntArray> ->
            val uri = getFaveArticlesContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeStore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            val indexes = IntArray(articles.size)
            for (i in articles.indices) {
                val dbo = articles[i]
                val cv = ContentValues()
                cv.put(FaveArticlesColumns.ARTICLE, GSON.toJson(dbo))
                val index = addToListAndReturnIndex(
                    operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
                indexes[i] = index
            }
            val results = contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            val ids = IntArray(results.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            e.onSuccess(ids)
        }
    }

    override fun storeProducts(
        accountId: Int,
        products: List<MarketEntity>,
        clearBeforeStore: Boolean
    ): Single<IntArray> {
        return Single.create { e: SingleEmitter<IntArray> ->
            val uri = getFaveProductsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeStore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            val indexes = IntArray(products.size)
            for (i in products.indices) {
                val dbo = products[i]
                val cv = ContentValues()
                cv.put(FaveProductColumns.PRODUCT, GSON.toJson(dbo))
                val index = addToListAndReturnIndex(
                    operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
                indexes[i] = index
            }
            val results = contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            val ids = IntArray(results.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            e.onSuccess(ids)
        }
    }

    override fun storePages(
        accountId: Int,
        users: List<FavePageEntity>,
        clearBeforeStore: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val uri = getFaveUsersContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeStore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            for (i in users.indices) {
                val dbo = users[i]
                val cv = createFaveCv(dbo)
                addToListAndReturnIndex(
                    operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            if (operations.isNotEmpty()) {
                contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            }
            e.onComplete()
        }
    }

    override fun storeGroups(
        accountId: Int,
        groups: List<FavePageEntity>,
        clearBeforeStore: Boolean
    ): Completable {
        return Completable.create { e: CompletableEmitter ->
            val uri = getFaveGroupsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBeforeStore) {
                operations.add(
                    ContentProviderOperation
                        .newDelete(uri)
                        .build()
                )
            }
            for (i in groups.indices) {
                val dbo = groups[i]
                val cv = createFaveCv(dbo)
                addToListAndReturnIndex(
                    operations, ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            if (operations.isNotEmpty()) {
                contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            }
            e.onComplete()
        }
    }

    private fun mapFaveLink(cursor: Cursor): FaveLinkEntity {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(FaveLinksColumns.LINK_ID))
        val url = cursor.getString(cursor.getColumnIndexOrThrow(FaveLinksColumns.URL))
        return FaveLinkEntity(id, url)
            .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(FaveLinksColumns.TITLE)))
            .setDescription(cursor.getString(cursor.getColumnIndexOrThrow(FaveLinksColumns.DESCRIPTION)))
            .setPhoto(mapFaveLinkPhoto(cursor))
    }

    private fun mapFavePosts(cursor: Cursor): PostEntity {
        val json = cursor.getString(cursor.getColumnIndexOrThrow(FavePostsColumns.POST))
        return GSON.fromJson(json, PostEntity::class.java)
    }

    companion object {
        private fun createFaveCv(dbo: FavePageEntity): ContentValues {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, dbo.id)
            cv.put(FavePageColumns.DESCRIPTION, dbo.description)
            cv.put(FavePageColumns.FAVE_TYPE, dbo.faveType)
            cv.put(FavePageColumns.UPDATED_TIME, dbo.updateDate)
            return cv
        }

        private fun mapUser(accountId: Int, id: Int): UserEntity? {
            return stores.owners().findUserDboById(accountId, id).blockingGet().get()
        }

        private fun mapGroup(accountId: Int, id: Int): CommunityEntity? {
            return stores.owners().findCommunityDboById(accountId, abs(id)).blockingGet().get()
        }

        private fun mapFaveUserDbo(cursor: Cursor, accountId: Int): FavePageEntity {
            return FavePageEntity(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                .setDescription(cursor.getString(cursor.getColumnIndexOrThrow(FavePageColumns.DESCRIPTION)))
                .setUpdateDate(cursor.getLong(cursor.getColumnIndexOrThrow(FavePageColumns.UPDATED_TIME)))
                .setFaveType(cursor.getString(cursor.getColumnIndexOrThrow(FavePageColumns.FAVE_TYPE)))
                .setUser(
                    mapUser(
                        accountId,
                        cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    )
                )
        }

        private fun mapFaveGroupDbo(cursor: Cursor, accountId: Int): FavePageEntity {
            return FavePageEntity(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                .setDescription(cursor.getString(cursor.getColumnIndexOrThrow(FavePageColumns.DESCRIPTION)))
                .setUpdateDate(cursor.getLong(cursor.getColumnIndexOrThrow(FavePageColumns.UPDATED_TIME)))
                .setFaveType(cursor.getString(cursor.getColumnIndexOrThrow(FavePageColumns.FAVE_TYPE)))
                .setGroup(
                    mapGroup(
                        accountId,
                        cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    )
                )
        }

        private fun mapFavePhoto(cursor: Cursor): PhotoEntity {
            val json = cursor.getString(cursor.getColumnIndexOrThrow(FavePhotosColumns.PHOTO))
            return GSON.fromJson(json, PhotoEntity::class.java)
        }

        private fun mapFaveLinkPhoto(cursor: Cursor): PhotoEntity {
            val json = cursor.getString(cursor.getColumnIndexOrThrow(FaveLinksColumns.PHOTO))
            return GSON.fromJson(json, PhotoEntity::class.java)
        }
    }
}