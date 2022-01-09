package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.FaveLink;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Video;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IFaveInteractor {
    Single<List<Post>> getPosts(int accountId, int count, int offset);

    Single<List<Post>> getCachedPosts(int accountId);

    Single<List<Photo>> getCachedPhotos(int accountId);

    Single<List<Photo>> getPhotos(int accountId, int count, int offset);

    Single<List<Video>> getCachedVideos(int accountId);

    Single<List<Article>> getCachedArticles(int accountId);

    Single<List<Market>> getCachedProducts(int accountId);

    Single<List<Market>> getProducts(int accountId, int count, int offset);

    Single<List<Video>> getVideos(int accountId, int count, int offset);

    Single<List<Article>> getArticles(int accountId, int count, int offset);

    Single<List<Article>> getOwnerPublishedArticles(int accountId, int ownerId, int count, int offset);

    Single<List<FavePage>> getCachedPages(int accountId, boolean isUser);

    Single<List<FavePage>> getPages(int accountId, int count, int offset, boolean isUser);

    Completable removePage(int accountId, int ownerId, boolean isUser);

    Single<List<FaveLink>> getCachedLinks(int accountId);

    Single<List<FaveLink>> getLinks(int accountId, int count, int offset);

    Completable removeLink(int accountId, String id);

    Single<Boolean> removeArticle(int accountId, Integer owner_id, Integer article_id);

    Single<Boolean> removeProduct(int accountId, Integer id, Integer owner_id);

    Completable addProduct(int accountId, int id, int owner_id, String access_key);

    Single<Boolean> removePost(int accountId, Integer owner_id, Integer id);

    Single<Boolean> removeVideo(int accountId, Integer owner_id, Integer id);

    Single<Boolean> pushFirst(int accountId, int owner_id);

    Completable addPage(int accountId, int ownerId);

    Completable addLink(int accountId, String link);

    Completable addVideo(int accountId, Integer owner_id, Integer id, String access_key);

    Completable addArticle(int accountId, String url);

    Completable addPost(int accountId, Integer owner_id, Integer id, String access_key);
}
