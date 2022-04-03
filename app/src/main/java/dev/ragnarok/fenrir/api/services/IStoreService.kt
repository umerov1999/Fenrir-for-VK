package dev.ragnarok.fenrir.api.services

import dev.ragnarok.fenrir.api.model.VkApiStickerSetsData
import dev.ragnarok.fenrir.api.model.VkApiStickersKeywords
import dev.ragnarok.fenrir.api.model.response.BaseResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IStoreService {
    @FormUrlEncoded
    @POST("execute")
    fun getStickers(@Field("code") code: String?): Single<BaseResponse<VkApiStickerSetsData>>

    @FormUrlEncoded
    @POST("execute")
    fun getStickersKeywords(@Field("code") code: String?): Single<BaseResponse<VkApiStickersKeywords>>
}