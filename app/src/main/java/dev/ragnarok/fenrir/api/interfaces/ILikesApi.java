package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import dev.ragnarok.fenrir.api.model.response.LikesListResponse;
import io.reactivex.rxjava3.core.Single;

public interface ILikesApi {

    @CheckResult
    Single<LikesListResponse> getList(String type, Integer ownerId, Integer itemId, String pageUrl, String filter,
                                      Boolean friendsOnly, Integer offset, Integer count, Boolean skipOwn, String fields);

    @CheckResult
    Single<Integer> delete(String type, Integer ownerId, int itemId, String accessKey);

    @CheckResult
    Single<Integer> add(String type, Integer ownerId, int itemId, String accessKey);

    @CheckResult
    Single<Boolean> isLiked(String type, Integer ownerId, int itemId);

    @CheckResult
    Single<Integer> checkAndAddLike(String type, Integer ownerId, int itemId, String accessKey);

}
