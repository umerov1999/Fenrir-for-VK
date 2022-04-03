package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.interfaces.IFriendsApi
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiUser
import dev.ragnarok.fenrir.api.model.VkApiFriendList
import dev.ragnarok.fenrir.api.model.response.DeleteFriendResponse
import dev.ragnarok.fenrir.api.model.response.MutualFriendsResponse
import dev.ragnarok.fenrir.api.model.response.OnlineFriendsResponse
import dev.ragnarok.fenrir.api.services.IFriendsService
import io.reactivex.rxjava3.core.Single

internal class FriendsApi(accountId: Int, provider: IServiceProvider) :
    AbsApi(accountId, provider), IFriendsApi {
    override fun getOnline(
        userId: Int,
        order: String?,
        count: Int,
        offset: Int,
        fields: String?
    ): Single<OnlineFriendsResponse> {
        val targetOrder = if (order == null) null else toQuotes(order)
        val targetFields = if (fields == null) null else toQuotes(fields)
        val code = """var user_id = %s;
var count = %s;
var offset = %s;
var fields = %s;

var uids = API.friends.getOnline({"v":"${Constants.API_VERSION}",
    "user_id":user_id, 
    "count":count, 
    "offset":offset,
    "order":%s
});

var profiles = API.users.get({"v":"${Constants.API_VERSION}","user_ids":uids, "fields":fields});

return {"uids":uids, "profiles":profiles};"""
        val formattedCode = String.format(code, userId, count, offset, targetFields, targetOrder)
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service
                    .getOnline(formattedCode)
                    .map(extractResponseWithErrorHandling())
            }
    }

    /*@Override
    public Single<FriendsWithCountersResponse> getWithCounters(int userId, String order, int count,
                                                               int offset, String fields) {
        String targetOrder = order == null ? null : toQuotes(order);
        String targetFields = fields == null ? null : toQuotes(fields);

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
    override fun get(
        userId: Int?, order: String?, listId: Int?, count: Int?,
        offset: Int?, fields: String?, nameCase: String?
    ): Single<Items<VKApiUser>> {
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service[userId, order, listId, count, offset, fields, nameCase]
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getByPhones(phones: String?, fields: String?): Single<List<VKApiUser>> {
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service.getByPhones(phones, fields)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getRecommendations(
        count: Int?,
        fields: String?,
        nameCase: String?
    ): Single<Items<VKApiUser>> {
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service.getRecommendations(count, fields, nameCase)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getLists(userId: Int?, returnSystem: Boolean?): Single<Items<VkApiFriendList>> {
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service.getLists(userId, integerFromBoolean(returnSystem))
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun delete(userId: Int): Single<DeleteFriendResponse> {
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service.delete(userId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun add(userId: Int, text: String?, follow: Boolean?): Single<Int> {
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service.add(userId, text, integerFromBoolean(follow))
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun search(
        userId: Int,
        query: String?,
        fields: String?,
        nameCase: String?,
        offset: Int?,
        count: Int?
    ): Single<Items<VKApiUser>> {
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service.search(userId, query, fields, nameCase, offset, count)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getMutual(
        sourceUid: Int?,
        targetUid: Int,
        count: Int,
        offset: Int,
        fields: String?
    ): Single<List<VKApiUser>> {
        val code = """var source_uid = %s;
var target_uid = %s;
var count = %s;
var offset = %s;
var fields = %s;

var uids = API.friends.getMutual({"v":"${Constants.API_VERSION}",
    "source_uid":source_uid, 
    "target_uid":target_uid, 
    "count":count, 
    "offset":offset
});

var profiles = API.users.get({"v":"${Constants.API_VERSION}","user_ids":uids, "fields":fields});

return {"uids":uids, "profiles":profiles};"""
        val formattedCode =
            String.format(code, sourceUid, targetUid, count, offset, toQuotes(fields))

        //return executionService()
        //        .execute(formattedCode)
        //        .map(response -> {
        //            MutualFriendsResponse data = convertJsonResponse(response.get(), MutualFriendsResponse.class);
        //            return data.profiles;
        //        });
        return provideService(IFriendsService::class.java)
            .flatMap { service: IFriendsService ->
                service.getMutual(formattedCode)
                    .map(extractResponseWithErrorHandling())
                    .map { response: MutualFriendsResponse -> response.profiles }
            }
    }
}