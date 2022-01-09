package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.VKApiWikiPage;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IPagesService {

    //https://vk.com/dev/pages.get
    @FormUrlEncoded
    @POST("pages.get")
    Single<BaseResponse<VKApiWikiPage>> get(@Field("owner_id") int ownerId,
                                            @Field("page_id") int pageId,
                                            @Field("global") Integer global,
                                            @Field("site_preview") Integer sitePreview,
                                            @Field("title") String title,
                                            @Field("need_source") Integer needSource,
                                            @Field("need_html") Integer needHtml);

}
