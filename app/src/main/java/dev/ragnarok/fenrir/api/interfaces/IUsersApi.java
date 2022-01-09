package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiGift;
import dev.ragnarok.fenrir.api.model.VKApiStory;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.response.StoryResponse;
import dev.ragnarok.fenrir.api.model.server.VkApiStoryUploadServer;
import io.reactivex.rxjava3.core.Single;


public interface IUsersApi {

    @CheckResult
    Single<VKApiUser> getUserWallInfo(int userId, String fields, String nameCase);

    @CheckResult
    Single<Items<VKApiUser>> getFollowers(Integer userId, Integer offset, Integer count,
                                          String fields, String nameCase);

    @CheckResult
    Single<Items<VKApiUser>> getRequests(Integer offset, Integer count, Integer extended, Integer out, String fields);

    @CheckResult
    Single<Items<VKApiUser>> search(String query, Integer sort, Integer offset, Integer count,
                                    String fields, Integer city, Integer country, String hometown,
                                    Integer universityCountry, Integer university, Integer universityYear,
                                    Integer universityFaculty, Integer universityChair, Integer sex,
                                    Integer status, Integer ageFrom, Integer ageTo, Integer birthDay,
                                    Integer birthMonth, Integer birthYear, Boolean online,
                                    Boolean hasPhoto, Integer schoolCountry, Integer schoolCity,
                                    Integer schoolClass, Integer school, Integer schoolYear,
                                    String religion, String interests, String company,
                                    String position, Integer groupId, String fromList);

    @CheckResult
    Single<List<VKApiUser>> get(Collection<Integer> userIds, Collection<String> domains,
                                String fields, String nameCase);

    @CheckResult
    Single<VkApiStoryUploadServer> stories_getPhotoUploadServer();

    @CheckResult
    Single<VkApiStoryUploadServer> stories_getVideoUploadServer();

    @CheckResult
    Single<Items<VKApiStory>> stories_save(String upload_results);

    @CheckResult
    Single<StoryResponse> getStory(Integer owner_id, Integer extended, String fields);

    @CheckResult
    Single<Integer> checkAndAddFriend(Integer userId);

    @CheckResult
    Single<Items<VKApiGift>> getGifts(Integer user_id, Integer count, Integer offset);

    @CheckResult
    Single<StoryResponse> searchStory(String q, Integer mentioned_id, Integer count, Integer extended, String fields);

    @CheckResult
    Single<Integer> report(Integer userId, String type, String comment);

}
