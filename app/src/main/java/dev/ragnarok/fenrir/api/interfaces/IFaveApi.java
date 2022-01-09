package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.List;

import dev.ragnarok.fenrir.api.model.FaveLinkDto;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiArticle;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VkApiMarket;
import dev.ragnarok.fenrir.api.model.response.FavePageResponse;
import dev.ragnarok.fenrir.api.model.response.FavePostsResponse;
import io.reactivex.rxjava3.core.Single;


public interface IFaveApi {

    @CheckResult
    Single<Items<FavePageResponse>> getPages(Integer offset, Integer count, String fields, String type);

    @CheckResult
    Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count);

    @CheckResult
    Single<List<VKApiVideo>> getVideos(Integer offset, Integer count);

    @CheckResult
    Single<List<VKApiArticle>> getArticles(Integer offset, Integer count);

    @CheckResult
    Single<List<VkApiMarket>> getProducts(Integer offset, Integer count);

    @CheckResult
    Single<Items<VKApiArticle>> getOwnerPublishedArticles(Integer owner_id, Integer offset, Integer count);

    @CheckResult
    Single<FavePostsResponse> getPosts(Integer offset, Integer count);

    @CheckResult
    Single<Items<FaveLinkDto>> getLinks(Integer offset, Integer count);

    @CheckResult
    Single<Boolean> addPage(Integer userId, Integer groupId);

    @CheckResult
    Single<Boolean> addLink(String link);

    @CheckResult
    Single<Boolean> removePage(Integer userId, Integer groupId);

    @CheckResult
    Single<Boolean> removeLink(String linkId);

    @CheckResult
    Single<Boolean> removeArticle(Integer owner_id, Integer article_id);

    @CheckResult
    Single<Boolean> removePost(Integer owner_id, Integer id);

    @CheckResult
    Single<Boolean> removeVideo(Integer owner_id, Integer id);

    @CheckResult
    Single<Boolean> pushFirst(int owner_id);

    @CheckResult
    Single<Boolean> addVideo(Integer owner_id, Integer id, String access_key);

    @CheckResult
    Single<Boolean> addArticle(String url);

    @CheckResult
    Single<Boolean> addProduct(int id, int owner_id, String access_key);

    @CheckResult
    Single<Boolean> removeProduct(Integer id, Integer owner_id);

    @CheckResult
    Single<Boolean> addPost(Integer owner_id, Integer id, String access_key);

}
