package dev.ragnarok.fenrir.api.impl;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.interfaces.IFriendsApi;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiFriendList;
import dev.ragnarok.fenrir.api.model.response.DeleteFriendResponse;
import dev.ragnarok.fenrir.api.model.response.OnlineFriendsResponse;
import dev.ragnarok.fenrir.api.services.IFriendsService;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Single;


class FriendsApi extends AbsApi implements IFriendsApi {

    FriendsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<OnlineFriendsResponse> getOnline(int userId, String order, int count, int offset, String fields) {
        String targetOrder = Objects.isNull(order) ? null : toQuotes(order);
        String targetFields = Objects.isNull(fields) ? null : toQuotes(fields);

        String code = "var user_id = %s;\n" +
                "var count = %s;\n" +
                "var offset = %s;\n" +
                "var fields = %s;\n" +
                "\n" +
                "var uids = API.friends.getOnline({\"v\":\"" + Constants.API_VERSION + "\",\n" +
                "    \"user_id\":user_id, \n" +
                "    \"count\":count, \n" +
                "    \"offset\":offset,\n" +
                "    \"order\":%s\n" +
                "});\n" +
                "\n" +
                "var profiles = API.users.get({\"v\":\"" + Constants.API_VERSION + "\",\"user_ids\":uids, \"fields\":fields});\n" +
                "\n" +
                "return {\"uids\":uids, \"profiles\":profiles};";

        String formattedCode = String.format(code, userId, count, offset, targetFields, targetOrder);
        return provideService(IFriendsService.class)
                .flatMap(service -> service
                        .getOnline(formattedCode)
                        .map(extractResponseWithErrorHandling()));
    }

    /*@Override
    public Single<FriendsWithCountersResponse> getWithCounters(int userId, String order, int count,
                                                               int offset, String fields) {
        String targetOrder = Objects.isNull(order) ? null : toQuotes(order);
        String targetFields = Objects.isNull(fields) ? null : toQuotes(fields);

        String code = "var friends = API.friends.get({" +
                "\"user_id\":" + userId + ", " +
                "\"fields\":" + targetFields + ", " +
                "\"order\":" + targetOrder + ", " +
                "\"count\":" + count + ", " +
                "\"offset\":" + offset + "}); " +

                "var counters = API.users.get({\"user_ids\":" + userId + ", \"fields\":\"counters\"})[0].counters; " +

                "return {\"friends\":friends, \"counters\":counters};";

        return provideService(IFriendsService.class)
                .flatMap(service -> service
                        .getWithMyCounters(code)
                        .map(extractResponseWithErrorHandling()));
    }*/

    @Override
    public Single<Items<VKApiUser>> get(Integer userId, String order, Integer listId, Integer count,
                                        Integer offset, String fields, String nameCase) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.get(userId, order, listId, count, offset, fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiUser>> getByPhones(String phones, String fields) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.getByPhones(phones, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> getRecommendations(Integer count, String fields, String nameCase) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.getRecommendations(count, fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiFriendList>> getLists(Integer userId, Boolean returnSystem) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.getLists(userId, integerFromBoolean(returnSystem))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<DeleteFriendResponse> delete(int userId) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.delete(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> add(int userId, String text, Boolean follow) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.add(userId, text, integerFromBoolean(follow))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> search(int userId, String query, String fields, String nameCase, Integer offset, Integer count) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.search(userId, query, fields, nameCase, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiUser>> getMutual(Integer sourceUid, int targetUid, int count, int offset, String fields) {
        String code = "var source_uid = %s;\n" +
                "var target_uid = %s;\n" +
                "var count = %s;\n" +
                "var offset = %s;\n" +
                "var fields = %s;\n" +
                "\n" +
                "var uids = API.friends.getMutual({\"v\":\"" + Constants.API_VERSION + "\",\n" +
                "    \"source_uid\":source_uid, \n" +
                "    \"target_uid\":target_uid, \n" +
                "    \"count\":count, \n" +
                "    \"offset\":offset\n" +
                "});\n" +
                "\n" +
                "var profiles = API.users.get({\"v\":\"" + Constants.API_VERSION + "\",\"user_ids\":uids, \"fields\":fields});\n" +
                "\n" +
                "return {\"uids\":uids, \"profiles\":profiles};";

        String formattedCode = String.format(code, sourceUid, targetUid, count, offset, toQuotes(fields));

        //return executionService()
        //        .execute(formattedCode)
        //        .map(response -> {
        //            MutualFriendsResponse data = convertJsonResponse(response.get(), MutualFriendsResponse.class);
        //            return data.profiles;
        //        });

        return provideService(IFriendsService.class)
                .flatMap(service -> service.getMutual(formattedCode)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response.profiles));
    }
}
