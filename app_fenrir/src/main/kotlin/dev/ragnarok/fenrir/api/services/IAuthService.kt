package dev.ragnarok.fenrir.api.services

import dev.ragnarok.fenrir.api.model.VKApiValidateAccount
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemCheckOtp
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemGetVerificationMethods
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemSendOtp
import dev.ragnarok.fenrir.api.model.response.AnonymTokenResponse
import dev.ragnarok.fenrir.api.model.response.BaseResponse
import dev.ragnarok.fenrir.api.model.response.GetAuthCodeStatusResponse
import dev.ragnarok.fenrir.api.model.response.LoginResponse
import dev.ragnarok.fenrir.api.model.response.SetAuthCodeStatusResponse
import dev.ragnarok.fenrir.api.model.response.VKUrlResponse
import dev.ragnarok.fenrir.api.rest.IServiceRest
import kotlinx.coroutines.flow.Flow

class IAuthService : IServiceRest() {
    fun directLogin(
        grantType: String?,
        clientId: Int,
        username: String?,
        password: String?,
        v: String?,
        twoFaSupported: Int?,
        scope: String?,
        captchaSuccessToken: String?,
        deviceId: String?,
        libVerifySupport: Int?,
        lang: String?,
        sid: String?,
        anonymousToken: String?,
        sakVersion: String?,
        flowType: String?
    ): Flow<LoginResponse> {
        return rest.request(
            "token",
            form(
                "libverify_support" to libVerifySupport,
                "scope" to scope,
                "sid" to sid,
                "grant_type" to grantType,
                "username" to username,
                "password" to password,
                "2fa_supported" to twoFaSupported,
                "anonymous_token" to anonymousToken,
                "https" to 1,
                "v" to v,
                "lang" to lang,
                "device_id" to deviceId,
                "sak_version" to sakVersion,
                "flow_type" to flowType,
                "api_id" to clientId,
                "success_token" to captchaSuccessToken
            ), LoginResponse.serializer(), false
        )
    }

    // initiator = expired_token
    // device_id - ads_android_id
    fun authByExchangeToken(
        clientId: Int,
        exchangeToken: String,
        scope: String,
        initiator: String,
        deviceId: String?,
        sakVersion: String?,
        v: String?,
        lang: String?
    ): Flow<VKUrlResponse> {
        return rest.requestAndGetURLFromRedirects(
            "auth_by_exchange_token",
            form(
                "client_id" to clientId,
                "api_id" to clientId,
                "exchange_token" to exchangeToken,
                "scope" to scope,
                "initiator" to initiator,
                "device_id" to deviceId,
                "sak_version" to sakVersion,
                "v" to v,
                "lang" to lang,
                "https" to 1
            )
        )
    }

    fun validateAccount(
        apiId: Int,
        lang: String?,
        deviceId: String?,
        supportedWays: String?,
        login: String,
        forcePassword: Int,
        passkeySupported: Int,
        sakVersion: String?,
        flowType: String?,
        accessToken: String?,
        v: String?,
    ): Flow<BaseResponse<VKApiValidateAccount>> {
        return rest.request(
            "auth.validateAccount",
            form(
                "lang" to lang,
                "device_id" to deviceId,
                "supported_ways" to supportedWays,
                "login" to login,
                "force_password" to forcePassword,
                "passkey_supported" to passkeySupported,
                "sak_version" to sakVersion,
                "flow_type" to flowType,
                "access_token" to accessToken,
                "v" to v,
                "https" to 1,
                "api_id" to apiId
            ),
            base(VKApiValidateAccount.serializer())
        )
    }

    //ecosystem.sendOtpSms
    //ecosystem.sendOtpPush
    //ecosystem.sendOtpCallReset
    //ecosystem.sendOtpEmail
    fun sendEcosystemOtp(
        apiId: Int,
        lang: String?,
        deviceId: String?,
        sid: String?,
        accessToken: String?,
        v: String?,
        suffix: String
    ): Flow<BaseResponse<EcosystemSendOtp>> {
        val finalSuffix = suffix.replaceFirstChar { it.uppercase() }
        return rest.request(
            "ecosystem.sendOtp$finalSuffix",
            form(
                "lang" to lang,
                "device_id" to deviceId,
                "sid" to sid,
                "access_token" to accessToken,
                "v" to v,
                "https" to 1,
                "api_id" to apiId
            ),
            base(EcosystemSendOtp.serializer())
        )
    }

    fun checkEcosystemOtp(
        apiId: Int,
        lang: String?,
        deviceId: String?,
        sid: String?,
        accessToken: String?,
        v: String?,
        verificationMethod: String,
        code: String
    ): Flow<BaseResponse<EcosystemCheckOtp>> {
        return rest.request(
            "ecosystem.checkOtp",
            form(
                "lang" to lang,
                "device_id" to deviceId,
                "sid" to sid,
                "access_token" to accessToken,
                "v" to v,
                "https" to 1,
                "api_id" to apiId,
                "code" to code,
                "verification_method" to verificationMethod
            ),
            base(EcosystemCheckOtp.serializer())
        )
    }

    fun getEcosystemVerificationMethods(
        apiId: Int,
        lang: String?,
        deviceId: String?,
        sid: String?,
        accessToken: String?,
        v: String?
    ): Flow<BaseResponse<EcosystemGetVerificationMethods>> {
        return rest.request(
            "ecosystem.getVerificationMethods",
            form(
                "lang" to lang,
                "device_id" to deviceId,
                "sid" to sid,
                "v" to v,
                "access_token" to accessToken,
                "https" to 1,
                "api_id" to apiId
            ),
            base(EcosystemGetVerificationMethods.serializer())
        )
    }

    fun getAnonymToken(
        clientId: Int,
        clientSecret: String?,
        v: String?,
        deviceId: String?,
        lang: String?
    ): Flow<AnonymTokenResponse> {
        return rest.request(
            "get_anonym_token",
            form(
                "api_id" to clientId,
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "v" to v,
                "device_id" to deviceId,
                "lang" to lang,
                "https" to 1
            ), AnonymTokenResponse.serializer()
        )
    }

    fun setAuthCodeStatus(
        authCode: String?,
        apiId: Int,
        deviceId: String?,
        accessToken: String?,
        lang: String?,
        v: String?
    ): Flow<BaseResponse<SetAuthCodeStatusResponse>> {
        return rest.request(
            "auth.setAuthCodeStatus",
            form(
                "api_id" to apiId,
                "auth_code" to authCode,
                "device_id" to deviceId,
                "access_token" to accessToken,
                "lang" to lang,
                "https" to 1,
                "v" to v
            ), base(SetAuthCodeStatusResponse.serializer())
        )
    }

    fun getAuthCodeStatus(
        authCode: String?,
        apiId: Int,
        deviceId: String?,
        accessToken: String?,
        lang: String?,
        v: String?
    ): Flow<BaseResponse<GetAuthCodeStatusResponse>> {
        return rest.request(
            "auth.getAuthCodeStatus",
            form(
                "api_id" to apiId,
                "auth_code" to authCode,
                "device_id" to deviceId,
                "access_token" to accessToken,
                "lang" to lang,
                "https" to 1,
                "v" to v
            ), base(GetAuthCodeStatusResponse.serializer())
        )
    }

    /*
    fun refreshTokens(
        clientId: Int,
        clientSecret: String,
        device_id: String,
        lang: String?,
        scope: String,
        initiator: String?,
        exchange_token: String,
        active_index: Int?,
        v: String?
        ): Single<LoginResponse> {
        return rest.request(
            "auth.refreshTokens",
            form(
                "v" to v,
                "scope" to scope,
                "client_secret" to clientSecret,
                "client_id" to clientId,
                "api_id" to clientId,
                "initiator" to initiator,
                "device_id" to device_id,
                "exchange_tokens" to exchange_token,
                "active_index" to active_index,
                "lang" to lang,
                "https" to 1
            ), RefreshExpiredTokenResponse.serializer(), false
        )
    }

    {
    "response": {
        "success": [{
            "index": 0,
            "user_id": 22*,
            "banned": false,
            "access_token": {
                "token": "vk1.a.",
                "expires_in": 0
            },
            "webview_access_token": {
                "token": "",
                "expires_in": 1695726230
            },
            "webview_refresh_token": {
                "token": "",
                "expires_in": 1698231830
            }
        }],
        "errors": [{
            "index": 1,
            "code": 5,
            "description": "User authorization failed"
        }]
    }
}
     */
}
