package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import dev.ragnarok.fenrir.api.model.CountersDto;
import dev.ragnarok.fenrir.api.model.RefreshToken;
import dev.ragnarok.fenrir.api.model.VkApiProfileInfo;
import dev.ragnarok.fenrir.api.model.VkApiProfileInfoResponce;
import dev.ragnarok.fenrir.api.model.response.AccountsBannedResponce;
import dev.ragnarok.fenrir.api.model.response.PushSettingsResponse;
import io.reactivex.rxjava3.core.Single;


public interface IAccountApi {

    @CheckResult
    Single<Integer> banUser(int userId);

    @CheckResult
    Single<Integer> unbanUser(int userId);

    Single<AccountsBannedResponce> getBanned(Integer count, Integer offset, String fields);

    @CheckResult
    Single<Boolean> unregisterDevice(String deviceId);

    @CheckResult
    Single<Boolean> registerDevice(String token, Integer pushes_granted, String app_version, String push_provider, String companion_apps,
                                   Integer type, String deviceModel, String deviceId, String systemVersion, String settings);

    @CheckResult
    Single<Boolean> setOffline();

    @CheckResult
    Single<VkApiProfileInfo> getProfileInfo();

    @CheckResult
    Single<PushSettingsResponse> getPushSettings();

    @CheckResult
    Single<VkApiProfileInfoResponce> saveProfileInfo(String first_name, String last_name, String maiden_name, String screen_name, String bdate, String home_town, Integer sex);

    @CheckResult
    Single<CountersDto> getCounters(String filter);

    @CheckResult
    Single<RefreshToken> refreshToken(String receipt, String receipt2, String nonce, Long timestamp);
}
