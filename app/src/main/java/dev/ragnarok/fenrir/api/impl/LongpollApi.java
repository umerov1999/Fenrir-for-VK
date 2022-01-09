package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IOtherVkRetrofitProvider;
import dev.ragnarok.fenrir.api.interfaces.ILongpollApi;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiGroupLongpollUpdates;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates;
import dev.ragnarok.fenrir.api.services.ILongpollUpdatesService;
import io.reactivex.rxjava3.core.Single;


public class LongpollApi implements ILongpollApi {

    private final IOtherVkRetrofitProvider provider;

    LongpollApi(IOtherVkRetrofitProvider provider) {
        this.provider = provider;
    }

    @Override
    public Single<VkApiLongpollUpdates> getUpdates(String server, String key, long ts, int wait, int mode, int version) {
        return provider.provideLongpollRetrofit()
                .flatMap(wrapper -> wrapper.create(ILongpollUpdatesService.class)
                        .getUpdates(server, "a_check", key, ts, wait, mode, version));
    }

    @Override
    public Single<VkApiGroupLongpollUpdates> getGroupUpdates(String server, String key, String ts, int wait) {
        return provider.provideLongpollRetrofit()
                .flatMap(wrapper -> wrapper.create(ILongpollUpdatesService.class)
                        .getGroupUpdates(server, "a_check", key, ts, wait));
    }
}