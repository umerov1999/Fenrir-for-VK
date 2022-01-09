package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.VkApiStickerSetsData;
import dev.ragnarok.fenrir.api.model.VkApiStickersKeywords;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IStoreService {

    @FormUrlEncoded
    @POST("execute")
    Single<BaseResponse<VkApiStickerSetsData>> getStickers(@Field("code") String code);

    @FormUrlEncoded
    @POST("execute")
    Single<BaseResponse<VkApiStickersKeywords>> getStickersKeywords(@Field("code") String code);
}