package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCheckedLink;
import dev.ragnarok.fenrir.api.model.VKApiShortLink;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.ResolveDomailResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiChatResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiLinkResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface IUtilsService {

    @FormUrlEncoded
    @POST("utils.resolveScreenName")
    Single<BaseResponse<ResolveDomailResponse>> resolveScreenName(@Field("screen_name") String screenName);

    @FormUrlEncoded
    @POST("utils.getShortLink")
    Single<BaseResponse<VKApiShortLink>> getShortLink(@Field("url") String url,
                                                      @Field("private") Integer t_private);

    @FormUrlEncoded
    @POST("utils.getLastShortenedLinks")
    Single<BaseResponse<Items<VKApiShortLink>>> getLastShortenedLinks(@Field("count") Integer count,
                                                                      @Field("offset") Integer offset);

    @FormUrlEncoded
    @POST("utils.deleteFromLastShortened")
    Single<BaseResponse<Integer>> deleteFromLastShortened(@Field("key") String key);

    @FormUrlEncoded
    @POST("utils.checkLink")
    Single<BaseResponse<VKApiCheckedLink>> checkLink(@Field("url") String url);

    @FormUrlEncoded
    @POST("messages.joinChatByInviteLink")
    Single<BaseResponse<VkApiChatResponse>> joinChatByInviteLink(@Field("link") String link);

    @FormUrlEncoded
    @POST("messages.getInviteLink")
    Single<BaseResponse<VkApiLinkResponse>> getInviteLink(@Field("peer_id") Integer peer_id,
                                                          @Field("reset") Integer reset);

    @FormUrlEncoded
    @POST("execute")
    Single<BaseResponse<Integer>> customScript(@Field("code") String code);
}
