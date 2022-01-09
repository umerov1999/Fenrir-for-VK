package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ILocalServerService {

    @FormUrlEncoded
    @POST("audio.get")
    Single<BaseResponse<Items<VKApiAudio>>> getAudios(@Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("discography.get")
    Single<BaseResponse<Items<VKApiAudio>>> getDiscography(@Field("offset") Integer offset,
                                                           @Field("count") Integer count,
                                                           @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("photos.get")
    Single<BaseResponse<Items<VKApiPhoto>>> getPhotos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("video.get")
    Single<BaseResponse<Items<VKApiVideo>>> getVideos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("audio.search")
    Single<BaseResponse<Items<VKApiAudio>>> searchAudios(@Field("q") String query,
                                                         @Field("offset") Integer offset,
                                                         @Field("count") Integer count,
                                                         @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("discography.search")
    Single<BaseResponse<Items<VKApiAudio>>> searchDiscography(@Field("q") String query,
                                                              @Field("offset") Integer offset,
                                                              @Field("count") Integer count,
                                                              @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("video.search")
    Single<BaseResponse<Items<VKApiVideo>>> searchVideos(@Field("q") String query,
                                                         @Field("offset") Integer offset,
                                                         @Field("count") Integer count,
                                                         @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("photos.search")
    Single<BaseResponse<Items<VKApiPhoto>>> searchPhotos(@Field("q") String query,
                                                         @Field("offset") Integer offset,
                                                         @Field("count") Integer count,
                                                         @Field("reverse") Integer reverse);

    @FormUrlEncoded
    @POST("update_time")
    Single<BaseResponse<Integer>> update_time(@Field("hash") String hash);

    @FormUrlEncoded
    @POST("delete_media")
    Single<BaseResponse<Integer>> delete_media(@Field("hash") String hash);

    @FormUrlEncoded
    @POST("get_file_name")
    Single<BaseResponse<String>> get_file_name(@Field("hash") String hash);

    @FormUrlEncoded
    @POST("update_file_name")
    Single<BaseResponse<Integer>> update_file_name(@Field("hash") String hash, @Field("name") String name);
}
