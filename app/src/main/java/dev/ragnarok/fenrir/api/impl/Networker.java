package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IOtherVkRetrofitProvider;
import dev.ragnarok.fenrir.api.IUploadRetrofitProvider;
import dev.ragnarok.fenrir.api.IVkRetrofitProvider;
import dev.ragnarok.fenrir.api.OtherVkRetrofitProvider;
import dev.ragnarok.fenrir.api.UploadRetrofitProvider;
import dev.ragnarok.fenrir.api.VkMethodHttpClientFactory;
import dev.ragnarok.fenrir.api.VkRetrofitProvider;
import dev.ragnarok.fenrir.api.interfaces.IAccountApis;
import dev.ragnarok.fenrir.api.interfaces.IAuthApi;
import dev.ragnarok.fenrir.api.interfaces.ILocalServerApi;
import dev.ragnarok.fenrir.api.interfaces.ILongpollApi;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.interfaces.IUploadApi;
import dev.ragnarok.fenrir.api.services.IAuthService;
import dev.ragnarok.fenrir.api.services.ILocalServerService;
import dev.ragnarok.fenrir.settings.IProxySettings;

public class Networker implements INetworker {

    private final IOtherVkRetrofitProvider otherVkRetrofitProvider;
    private final IVkRetrofitProvider vkRetrofitProvider;
    private final IUploadRetrofitProvider uploadRetrofitProvider;

    public Networker(IProxySettings settings) {
        otherVkRetrofitProvider = new OtherVkRetrofitProvider(settings);
        vkRetrofitProvider = new VkRetrofitProvider(settings, new VkMethodHttpClientFactory());
        uploadRetrofitProvider = new UploadRetrofitProvider(settings);
    }

    @Override
    public IAccountApis vkDefault(int accountId) {
        return VkApies.get(accountId, vkRetrofitProvider);
    }

    @Override
    public IAccountApis vkManual(int accountId, String accessToken) {
        return VkApies.create(accountId, accessToken, vkRetrofitProvider);
    }

    @Override
    public IAuthApi vkDirectAuth() {
        return new AuthApi(() -> otherVkRetrofitProvider.provideAuthRetrofit().map(wrapper -> wrapper.create(IAuthService.class)));
    }

    @Override
    public IAuthApi vkAuth() {
        return new AuthApi(() -> otherVkRetrofitProvider.provideAuthServiceRetrofit().map(wrapper -> wrapper.create(IAuthService.class)));
    }

    @Override
    public ILocalServerApi localServerApi() {
        return new LocalServerApi(() -> otherVkRetrofitProvider.provideLocalServerRetrofit().map(wrapper -> wrapper.create(ILocalServerService.class)));
    }

    @Override
    public ILongpollApi longpoll() {
        return new LongpollApi(otherVkRetrofitProvider);
    }

    @Override
    public IUploadApi uploads() {
        return new UploadApi(uploadRetrofitProvider);
    }
}
