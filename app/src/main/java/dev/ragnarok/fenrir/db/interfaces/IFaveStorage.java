package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.ArticleEntity;
import dev.ragnarok.fenrir.db.model.entity.FaveLinkEntity;
import dev.ragnarok.fenrir.db.model.entity.FavePageEntity;
import dev.ragnarok.fenrir.db.model.entity.MarketEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoEntity;
import dev.ragnarok.fenrir.model.criteria.FaveArticlesCriteria;
import dev.ragnarok.fenrir.model.criteria.FavePhotosCriteria;
import dev.ragnarok.fenrir.model.criteria.FavePostsCriteria;
import dev.ragnarok.fenrir.model.criteria.FaveProductsCriteria;
import dev.ragnarok.fenrir.model.criteria.FaveVideosCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IFaveStorage extends IStorage {

    @CheckResult
    Single<List<PostEntity>> getFavePosts(@NonNull FavePostsCriteria criteria);

    @CheckResult
    Completable storePosts(int accountId, List<PostEntity> posts, OwnerEntities owners, boolean clearBeforeStore);

    @CheckResult
    Single<List<FaveLinkEntity>> getFaveLinks(int accountId);

    Completable removeLink(int accountId, String id);

    Completable storeLinks(int accountId, List<FaveLinkEntity> entities, boolean clearBefore);

    @CheckResult
    Completable storePages(int accountId, List<FavePageEntity> users, boolean clearBeforeStore);

    Single<List<FavePageEntity>> getFaveUsers(int accountId);

    Completable removePage(int accountId, int ownerId, boolean isUser);

    @CheckResult
    Single<int[]> storePhotos(int accountId, List<PhotoEntity> photos, boolean clearBeforeStore);

    @CheckResult
    Single<List<PhotoEntity>> getPhotos(FavePhotosCriteria criteria);

    @CheckResult
    Single<List<VideoEntity>> getVideos(FaveVideosCriteria criteria);

    @CheckResult
    Single<List<ArticleEntity>> getArticles(FaveArticlesCriteria criteria);

    @CheckResult
    Single<List<MarketEntity>> getProducts(FaveProductsCriteria criteria);

    @CheckResult
    Single<int[]> storeVideos(int accountId, List<VideoEntity> videos, boolean clearBeforeStore);

    @CheckResult
    Single<int[]> storeArticles(int accountId, List<ArticleEntity> articles, boolean clearBeforeStore);

    @CheckResult
    Single<int[]> storeProducts(int accountId, List<MarketEntity> products, boolean clearBeforeStore);

    Single<List<FavePageEntity>> getFaveGroups(int accountId);

    Completable storeGroups(int accountId, List<FavePageEntity> groups, boolean clearBeforeStore);
}
