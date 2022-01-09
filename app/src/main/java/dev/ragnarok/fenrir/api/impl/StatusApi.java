package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IStatusApi;
import dev.ragnarok.fenrir.api.services.IStatusService;
import io.reactivex.rxjava3.core.Single;


class StatusApi extends AbsApi implements IStatusApi {

    StatusApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Boolean> set(String text, Integer groupId) {
        return provideService(IStatusService.class, TokenType.USER)
                .flatMap(service -> service.set(text, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }
}
