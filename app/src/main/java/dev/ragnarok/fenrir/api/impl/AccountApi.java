package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IAccountApi;
import dev.ragnarok.fenrir.api.model.CountersDto;
import dev.ragnarok.fenrir.api.model.RefreshToken;
import dev.ragnarok.fenrir.api.model.VkApiProfileInfo;
import dev.ragnarok.fenrir.api.model.VkApiProfileInfoResponce;
import dev.ragnarok.fenrir.api.model.response.AccountsBannedResponce;
import dev.ragnarok.fenrir.api.model.response.PushSettingsResponse;
import dev.ragnarok.fenrir.api.services.IAccountService;
import io.reactivex.rxjava3.core.Single;


class AccountApi extends AbsApi implements IAccountApi {

    AccountApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Integer> banUser(int userId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .banUser(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> unbanUser(int userId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .unbanUser(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<AccountsBannedResponce> getBanned(Integer count, Integer offset, String fields) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getBanned(count, offset, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> unregisterDevice(String deviceId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service.unregisterDevice(deviceId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> registerDevice(String token, Integer pushes_granted, String app_version, String push_provider, String companion_apps,
                                          Integer type, String deviceModel, String deviceId, String systemVersion, String settings) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .registerDevice(token, pushes_granted, app_version, push_provider, companion_apps, type, deviceModel, deviceId, systemVersion, settings)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> setOffline() {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .setOffline()
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<VkApiProfileInfo> getProfileInfo() {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getProfileInfo()
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<PushSettingsResponse> getPushSettings() {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getPushSettings()
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiProfileInfoResponce> saveProfileInfo(String first_name, String last_name, String maiden_name, String screen_name, String bdate, String home_town, Integer sex) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .saveProfileInfo(first_name, last_name, maiden_name, screen_name, bdate, home_town, sex)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<CountersDto> getCounters(String filter) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getCounters(filter)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<RefreshToken> refreshToken(String receipt, String receipt2, String nonce, Long timestamp) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .refreshToken(receipt, receipt2, nonce, timestamp)
                        .map(extractResponseWithErrorHandling()));
    }
}
