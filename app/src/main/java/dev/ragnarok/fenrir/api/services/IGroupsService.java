package dev.ragnarok.fenrir.api.services;

import java.util.List;

import dev.ragnarok.fenrir.api.model.GroupSettingsDto;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiGroupChats;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiBanned;
import dev.ragnarok.fenrir.api.model.VkApiMarket;
import dev.ragnarok.fenrir.api.model.VkApiMarketAlbum;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.GroupLongpollServer;
import dev.ragnarok.fenrir.api.model.response.GroupWallInfoResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IGroupsService {

    @FormUrlEncoded
    @POST("groups.editManager")
    Single<BaseResponse<Integer>> editManager(@Field("group_id") int groupId,
                                              @Field("user_id") int userId,
                                              @Field("role") String role,
                                              @Field("is_contact") Integer isContact,
                                              @Field("contact_position") String contactPosition,
                                              @Field("contact_phone") String contactPhone,
                                              @Field("contact_email") String contactEmail);

    @FormUrlEncoded
    @POST("groups.unban")
    Single<BaseResponse<Integer>> unban(@Field("group_id") int groupId,
                                        @Field("owner_id") int ownerId);

    @FormUrlEncoded
    @POST("market.getAlbums")
    Single<BaseResponse<Items<VkApiMarketAlbum>>> getMarketAlbums(@Field("owner_id") int owner_id,
                                                                  @Field("offset") int offset,
                                                                  @Field("count") int count);

    @FormUrlEncoded
    @POST("market.get")
    Single<BaseResponse<Items<VkApiMarket>>> getMarket(@Field("owner_id") int owner_id,
                                                       @Field("album_id") int album_id,
                                                       @Field("offset") int offset,
                                                       @Field("count") int count,
                                                       @Field("extended") Integer extended);

    @FormUrlEncoded
    @POST("market.getById")
    Single<BaseResponse<Items<VkApiMarket>>> getMarketById(@Field("item_ids") String item_ids,
                                                           @Field("extended") Integer extended);

    @POST("groups.ban")
    @FormUrlEncoded
    Single<BaseResponse<Integer>> ban(@Field("group_id") int groupId,
                                      @Field("owner_id") int ownerId,
                                      @Field("end_date") Long endDate,
                                      @Field("reason") Integer reason,
                                      @Field("comment") String comment,
                                      @Field("comment_visible") Integer commentVisible);

    @FormUrlEncoded
    @POST("groups.getSettings")
    Single<BaseResponse<GroupSettingsDto>> getSettings(@Field("group_id") int groupId);

    //https://vk.com/dev/groups.getBanned
    @FormUrlEncoded
    @POST("groups.getBanned")
    Single<BaseResponse<Items<VkApiBanned>>> getBanned(@Field("group_id") int groupId,
                                                       @Field("offset") Integer offset,
                                                       @Field("count") Integer count,
                                                       @Field("fields") String fields,
                                                       @Field("user_id") Integer userId);

    @FormUrlEncoded
    @POST("execute")
    Single<BaseResponse<GroupWallInfoResponse>> getGroupWallInfo(@Field("code") String code,
                                                                 @Field("group_id") String groupId,
                                                                 @Field("fields") String fields);

    @FormUrlEncoded
    @POST("groups.getMembers")
    Single<BaseResponse<Items<VKApiUser>>> getMembers(@Field("group_id") String groupId,
                                                      @Field("sort") Integer sort,
                                                      @Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("fields") String fields,
                                                      @Field("filter") String filter);

    //https://vk.com/dev/groups.search
    @FormUrlEncoded
    @POST("groups.search")
    Single<BaseResponse<Items<VKApiCommunity>>> search(@Field("q") String query,
                                                       @Field("type") String type,
                                                       @Field("fields") String fields,
                                                       @Field("country_id") Integer countryId,
                                                       @Field("city_id") Integer cityId,
                                                       @Field("future") Integer future,
                                                       @Field("market") Integer market,
                                                       @Field("sort") Integer sort,
                                                       @Field("offset") Integer offset,
                                                       @Field("count") Integer count);

    @FormUrlEncoded
    @POST("groups.getLongPollServer")
    Single<BaseResponse<GroupLongpollServer>> getLongPollServer(@Field("group_id") int groupId);

    //https://vk.com/dev/groups.leave
    @FormUrlEncoded
    @POST("groups.leave")
    Single<BaseResponse<Integer>> leave(@Field("group_id") int groupId);

    //https://vk.com/dev/groups.join
    @FormUrlEncoded
    @POST("groups.join")
    Single<BaseResponse<Integer>> join(@Field("group_id") int groupId,
                                       @Field("not_sure") Integer notSure);

    //https://vk.com/dev/groups.get
    @FormUrlEncoded
    @POST("groups.get")
    Single<BaseResponse<Items<VKApiCommunity>>> get(@Field("user_id") Integer userId,
                                                    @Field("extended") Integer extended,
                                                    @Field("filter") String filter,
                                                    @Field("fields") String fields,
                                                    @Field("offset") Integer offset,
                                                    @Field("count") Integer count);

    /**
     * Returns information about communities by their IDs.
     *
     * @param groupIds IDs or screen names of communities.
     *                 List of comma-separated words
     * @param groupId  ID or screen name of the community
     * @param fields   Group fields to return. List of comma-separated words
     * @return an array of objects describing communities
     */
    @FormUrlEncoded
    @POST("groups.getById")
    Single<BaseResponse<List<VKApiCommunity>>> getById(@Field("group_ids") String groupIds,
                                                       @Field("group_id") String groupId,
                                                       @Field("fields") String fields);

    @FormUrlEncoded
    @POST("groups.getChats")
    Single<BaseResponse<Items<VKApiGroupChats>>> getChats(@Field("group_id") int groupId,
                                                          @Field("offset") Integer offset,
                                                          @Field("count") Integer count);

}
