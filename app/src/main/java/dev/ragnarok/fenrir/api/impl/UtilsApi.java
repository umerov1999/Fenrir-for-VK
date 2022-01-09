package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IUtilsApi;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCheckedLink;
import dev.ragnarok.fenrir.api.model.VKApiShortLink;
import dev.ragnarok.fenrir.api.model.response.ResolveDomailResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiChatResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiLinkResponse;
import dev.ragnarok.fenrir.api.services.IUtilsService;
import io.reactivex.rxjava3.core.Single;


class UtilsApi extends AbsApi implements IUtilsApi {

    UtilsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<ResolveDomailResponse> resolveScreenName(String screenName) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.resolveScreenName(screenName)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiShortLink> getShortLink(String url, Integer t_private) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.getShortLink(url, t_private)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiShortLink>> getLastShortenedLinks(Integer count, Integer offset) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.getLastShortenedLinks(count, offset)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> deleteFromLastShortened(String key) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.deleteFromLastShortened(key)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiCheckedLink> checkLink(String url) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.checkLink(url)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiChatResponse> joinChatByInviteLink(String link) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.joinChatByInviteLink(link)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiLinkResponse> getInviteLink(Integer peer_id, Integer reset) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.getInviteLink(peer_id, reset)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> customScript(String code) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.customScript(code)
                        .map(extractResponseWithErrorHandling()));
    }
}
