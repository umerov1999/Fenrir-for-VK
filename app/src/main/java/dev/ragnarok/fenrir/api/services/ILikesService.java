package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.LikeResponse;
import dev.ragnarok.fenrir.api.model.response.LikesListResponse;
import dev.ragnarok.fenrir.api.model.response.isLikeResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ILikesService {

    //https://vk.com/dev/likes.getList
    @FormUrlEncoded
    @POST("likes.getList")
    Single<BaseResponse<LikesListResponse>> getList(@Field("type") String type,
                                                    @Field("owner_id") Integer ownerId,
                                                    @Field("item_id") Integer itemId,
                                                    @Field("page_url") String pageUrl,
                                                    @Field("filter") String filter,
                                                    @Field("friends_only") Integer friendsOnly,
                                                    @Field("extended") Integer extended,
                                                    @Field("offset") Integer offset,
                                                    @Field("count") Integer count,
                                                    @Field("skip_own") Integer skipOwn,
                                                    @Field("fields") String fields);

    //https://vk.com/dev/likes.delete
    @FormUrlEncoded
    @POST("likes.delete")
    Single<BaseResponse<LikeResponse>> delete(@Field("type") String type,
                                              @Field("owner_id") Integer ownerId,
                                              @Field("item_id") int itemId,
                                              @Field("access_key") String accessKey);

    //https://vk.com/dev/likes.add
    @FormUrlEncoded
    @POST("likes.add")
    Single<BaseResponse<LikeResponse>> add(@Field("type") String type,
                                           @Field("owner_id") Integer ownerId,
                                           @Field("item_id") int itemId,
                                           @Field("access_key") String accessKey);

    //https://vk.com/dev/likes.isLiked
    @FormUrlEncoded
    @POST("likes.isLiked")
    Single<BaseResponse<isLikeResponse>> isLiked(@Field("type") String type,
                                                 @Field("owner_id") Integer ownerId,
                                                 @Field("item_id") int itemId);

    @FormUrlEncoded
    @POST("execute")
    Single<BaseResponse<Integer>> checkAndAddLike(@Field("code") String code,
                                                  @Field("type") String type,
                                                  @Field("owner_id") Integer ownerId,
                                                  @Field("item_id") int itemId,
                                                  @Field("access_key") String accessKey);

}
