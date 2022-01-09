package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.List;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiFriendList;
import dev.ragnarok.fenrir.api.model.response.DeleteFriendResponse;
import dev.ragnarok.fenrir.api.model.response.OnlineFriendsResponse;
import io.reactivex.rxjava3.core.Single;


public interface IFriendsApi {

    @CheckResult
    Single<OnlineFriendsResponse> getOnline(int userId, String order, int count,
                                            int offset, String fields);

    @CheckResult
    Single<Items<VKApiUser>> get(Integer userId, String order, Integer listId, Integer count, Integer offset,
                                 String fields, String nameCase);

    @CheckResult
    Single<List<VKApiUser>> getByPhones(String phones, String fields);

    @CheckResult
    Single<Items<VKApiUser>> getRecommendations(Integer count, String fields, String nameCase);

    @CheckResult
    Single<Items<VkApiFriendList>> getLists(Integer userId, Boolean returnSystem);

    @CheckResult
    Single<DeleteFriendResponse> delete(int userId);

    @CheckResult
    Single<Integer> add(int userId, String text, Boolean follow);

    @CheckResult
    Single<Items<VKApiUser>> search(int userId, String query, String fields, String nameCase,
                                    Integer offset, Integer count);

    @CheckResult
    Single<List<VKApiUser>> getMutual(Integer sourceUid, int targetUid, int count, int offset, String fields);

}
