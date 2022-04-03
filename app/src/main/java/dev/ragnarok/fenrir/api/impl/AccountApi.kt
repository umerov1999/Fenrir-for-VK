package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.TokenType
import dev.ragnarok.fenrir.api.interfaces.IAccountApi
import dev.ragnarok.fenrir.api.model.CountersDto
import dev.ragnarok.fenrir.api.model.RefreshToken
import dev.ragnarok.fenrir.api.model.VkApiProfileInfo
import dev.ragnarok.fenrir.api.model.VkApiProfileInfoResponce
import dev.ragnarok.fenrir.api.model.response.AccountsBannedResponce
import dev.ragnarok.fenrir.api.model.response.PushSettingsResponse
import dev.ragnarok.fenrir.api.services.IAccountService
import io.reactivex.rxjava3.core.Single

internal class AccountApi(accountId: Int, provider: IServiceProvider) :
    AbsApi(accountId, provider), IAccountApi {
    override fun banUser(userId: Int): Single<Int> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .banUser(userId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun unbanUser(userId: Int): Single<Int> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .unbanUser(userId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getBanned(
        count: Int?,
        offset: Int?,
        fields: String?
    ): Single<AccountsBannedResponce> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .getBanned(count, offset, fields)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun unregisterDevice(deviceId: String?): Single<Boolean> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service.unregisterDevice(deviceId)
                    .map(extractResponseWithErrorHandling())
                    .map { response: Int -> response == 1 }
            }
    }

    override fun registerDevice(
        token: String?,
        pushes_granted: Int?,
        app_version: String?,
        push_provider: String?,
        companion_apps: String?,
        type: Int?,
        deviceModel: String?,
        deviceId: String?,
        systemVersion: String?,
        settings: String?
    ): Single<Boolean> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .registerDevice(
                        token,
                        pushes_granted,
                        app_version,
                        push_provider,
                        companion_apps,
                        type,
                        deviceModel,
                        deviceId,
                        systemVersion,
                        settings
                    )
                    .map(extractResponseWithErrorHandling())
                    .map { response: Int -> response == 1 }
            }
    }

    override fun setOffline(): Single<Boolean> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .setOffline()
                    .map(extractResponseWithErrorHandling())
                    .map { response: Int -> response == 1 }
            }
    }

    override val profileInfo: Single<VkApiProfileInfo>
        get() = provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .profileInfo
                    .map(extractResponseWithErrorHandling())
            }
    override val pushSettings: Single<PushSettingsResponse>
        get() = provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .pushSettings
                    .map(extractResponseWithErrorHandling())
            }

    override fun saveProfileInfo(
        first_name: String?,
        last_name: String?,
        maiden_name: String?,
        screen_name: String?,
        bdate: String?,
        home_town: String?,
        sex: Int?
    ): Single<VkApiProfileInfoResponce> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .saveProfileInfo(
                        first_name,
                        last_name,
                        maiden_name,
                        screen_name,
                        bdate,
                        home_town,
                        sex
                    )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getCounters(filter: String?): Single<CountersDto> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .getCounters(filter)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun refreshToken(
        receipt: String?,
        receipt2: String?,
        nonce: String?,
        timestamp: Long?
    ): Single<RefreshToken> {
        return provideService(IAccountService::class.java, TokenType.USER)
            .flatMap { service: IAccountService ->
                service
                    .refreshToken(receipt, receipt2, nonce, timestamp)
                    .map(extractResponseWithErrorHandling())
            }
    }
}