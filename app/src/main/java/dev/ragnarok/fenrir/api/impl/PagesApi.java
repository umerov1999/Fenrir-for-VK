package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IPagesApi;
import dev.ragnarok.fenrir.api.model.VKApiWikiPage;
import dev.ragnarok.fenrir.api.services.IPagesService;
import io.reactivex.rxjava3.core.Single;


class PagesApi extends AbsApi implements IPagesApi {

    PagesApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<VKApiWikiPage> get(int ownerId, int pageId, Boolean global, Boolean sitePreview, String title, Boolean needSource, Boolean needHtml) {
        return provideService(IPagesService.class, TokenType.USER)
                .flatMap(service -> service
                        .get(ownerId, pageId, integerFromBoolean(global), integerFromBoolean(sitePreview),
                                title, integerFromBoolean(needSource), integerFromBoolean(needHtml))
                        .map(extractResponseWithErrorHandling()));
    }
}
