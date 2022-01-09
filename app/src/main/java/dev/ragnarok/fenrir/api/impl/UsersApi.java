package dev.ragnarok.fenrir.api.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IUsersApi;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiGift;
import dev.ragnarok.fenrir.api.model.VKApiStory;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.response.StoryResponse;
import dev.ragnarok.fenrir.api.model.response.UserWallInfoResponse;
import dev.ragnarok.fenrir.api.model.server.VkApiStoryUploadServer;
import dev.ragnarok.fenrir.api.services.IUsersService;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;


class UsersApi extends AbsApi implements IUsersApi {

    UsersApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    private static VKApiUser createFrom(UserWallInfoResponse response) {
        VKApiUser user = response.users.get(0);

        if (isNull(user.counters)) {
            user.counters = new VKApiUser.Counters();
        }

        if (nonNull(response.allWallCount)) {
            user.counters.all_wall = response.allWallCount;
        }

        if (nonNull(response.ownerWallCount)) {
            user.counters.owner_wall = response.ownerWallCount;
        }

        if (nonNull(response.postponedWallCount)) {
            user.counters.postponed_wall = response.postponedWallCount;
        }

        return user;
    }

    @Override
    public Single<VKApiUser> getUserWallInfo(int userId, String fields, String nameCase) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service
                        .getUserWallInfo("var user_id = Args.user_id;\n" +
                                "var fields =Args.fields;\n" +
                                "var name_case = Args.name_case;\n" +
                                "\n" +
                                "var user_info = API.users.get({\"v\":\"" + Constants.API_VERSION + "\",\"user_ids\":user_id,\n" +
                                "    \"fields\":fields, \"name_case\":name_case});\n" +
                                "\n" +
                                "var all_wall_count =API.wall.get({\"v\":\"" + Constants.API_VERSION + "\",\"owner_id\":user_id,\n" +
                                "    \"count\":1, \"filter\":\"all\"}).count;\n" +
                                "    \n" +
                                "var owner_wall_count =API.wall.get({\"v\":\"" + Constants.API_VERSION + "\",\"owner_id\":user_id,\n" +
                                "    \"count\":1, \"filter\":\"owner\"}).count;\n" +
                                "\n" +
                                "var postponed_wall_count =API.wall.get({\"v\":\"" + Constants.API_VERSION + "\",\"owner_id\":user_id,\n" +
                                "    \"count\":1, \"filter\":\"postponed\"}).count;\n" +
                                "\n" +
                                "return {\"user_info\": user_info, \n" +
                                "    \"all_wall_count\":all_wall_count,\n" +
                                "    \"owner_wall_count\":owner_wall_count,\n" +
                                "    \"postponed_wall_count\":postponed_wall_count\n" +
                                "};", userId, fields, nameCase)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            if (Utils.safeCountOf(response.users) != 1) {
                                throw new NotFoundException();
                            }

                            return createFrom(response);
                        }));
    }

    @Override
    public Single<Items<VKApiUser>> getFollowers(Integer userId, Integer offset, Integer count, String fields, String nameCase) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.getFollowers(userId, offset, count, fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> getRequests(Integer offset, Integer count, Integer extended, Integer out, String fields) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.getRequests(offset, count, extended, out, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> search(String query, Integer sort, Integer offset, Integer count, String fields, Integer city, Integer country, String hometown, Integer universityCountry, Integer university, Integer universityYear, Integer universityFaculty, Integer universityChair, Integer sex, Integer status, Integer ageFrom, Integer ageTo, Integer birthDay, Integer birthMonth, Integer birthYear, Boolean online, Boolean hasPhoto, Integer schoolCountry, Integer schoolCity, Integer schoolClass, Integer school, Integer schoolYear, String religion, String interests, String company, String position, Integer groupId, String fromList) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service
                        .search(query, sort, offset, count, fields, city, country, hometown, universityCountry,
                                university, universityYear, universityFaculty, universityChair, sex, status,
                                ageFrom, ageTo, birthDay, birthMonth, birthYear, integerFromBoolean(online),
                                integerFromBoolean(hasPhoto), schoolCountry, schoolCity, schoolClass, school,
                                schoolYear, religion, interests, company, position, groupId, fromList)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> report(Integer userId, String type, String comment) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service
                        .report(userId, type, comment)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> checkAndAddFriend(Integer userId) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service
                        .checkAndAddFriend("var user_id = Args.user_id; if(API.users.get({\"v\":\"" + Constants.API_VERSION + "\", \"user_ids\": user_id, \"fields\": \"friend_status\"})[0].friend_status == 0) {return API.friends.add({\"v\":\"" + Constants.API_VERSION + "\", \"user_id\": user_id});} return 0;", userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<StoryResponse> getStory(Integer owner_id, Integer extended, String fields) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.getStory(owner_id, extended, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiGift>> getGifts(Integer user_id, Integer count, Integer offset) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.getGifts(user_id, count, offset)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<StoryResponse> searchStory(String q, Integer mentioned_id, Integer count, Integer extended, String fields) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.searchStory(q, mentioned_id, count, extended, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiUser>> get(Collection<Integer> userIds, Collection<String> domains, String fields, String nameCase) {
        ArrayList<String> ids = new ArrayList<>(1);
        if (nonNull(userIds)) {
            ids.add(join(userIds, ","));
        }

        if (nonNull(domains)) {
            ids.add(join(domains, ","));
        }

        return provideService(IUsersService.class, TokenType.USER, TokenType.COMMUNITY, TokenType.SERVICE)
                .flatMap(service -> service
                        .get(join(ids, ","), fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiStoryUploadServer> stories_getPhotoUploadServer() {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.stories_getPhotoUploadServer(1)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiStoryUploadServer> stories_getVideoUploadServer() {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.stories_getVideoUploadServer(1)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiStory>> stories_save(String upload_results) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.stories_save(upload_results)
                        .map(extractResponseWithErrorHandling()));
    }
}
