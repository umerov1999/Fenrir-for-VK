package dev.ragnarok.fenrir.api.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.api.model.VKApiValidateAccount
import dev.ragnarok.fenrir.api.model.VKApiValidatePhone
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemCheckOtp
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemGetVerificationMethods
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemSendOtp
import dev.ragnarok.fenrir.api.model.response.AnonymTokenResponse
import dev.ragnarok.fenrir.api.model.response.GetAuthCodeStatusResponse
import dev.ragnarok.fenrir.api.model.response.LoginResponse
import dev.ragnarok.fenrir.api.model.response.SetAuthCodeStatusResponse
import dev.ragnarok.fenrir.api.model.response.VKUrlResponse
import kotlinx.coroutines.flow.Flow

interface IAuthApi {
    @CheckResult
    fun directLogin(
        grantType: String?,
        clientId: Int,
        username: String?,
        password: String?,
        code: String?,
        v: String?,
        twoFaSupported: Boolean,
        scope: String?,
        captchaSuccessToken: String?,
        libVerifySupport: Boolean,
        sid: String?,
        anonymousToken: String?,
        sakVersion: String?,
        flowType: String?
    ): Flow<LoginResponse>

    @CheckResult
    fun authByExchangeToken(
        clientId: Int,
        exchangeToken: String,
        scope: String,
        initiator: String,
        deviceId: String?,
        sakVersion: String?,
        v: String?
    ): Flow<VKUrlResponse>

    @CheckResult
    fun validateAccount(
        apiId: Int,
        supportedWays: String?,
        login: String,
        forcePassword: Boolean,
        passkeySupported: Boolean,
        sakVersion: String?,
        flowType: String?,
        accessToken: String?,
        v: String?
    ): Flow<VKApiValidateAccount>

    @CheckResult
    fun validatePhone(
        phone: String?,
        apiId: Int,
        sid: String?,
        v: String?,
        libVerifySupport: Boolean,
        allowCallReset: Boolean
    ): Flow<VKApiValidatePhone>

    @CheckResult
    fun sendEcosystemOtp(
        apiId: Int,
        sid: String?,
        accessToken: String?,
        v: String?,
        suffix: String
    ): Flow<EcosystemSendOtp>

    @CheckResult
    fun checkEcosystemOtp(
        apiId: Int,
        sid: String?,
        accessToken: String?,
        v: String?,
        verificationMethod: String,
        code: String
    ): Flow<EcosystemCheckOtp>

    @CheckResult
    fun getEcosystemVerificationMethods(
        apiId: Int,
        sid: String?,
        accessToken: String?,
        v: String?
    ): Flow<EcosystemGetVerificationMethods>

    @CheckResult
    fun getAnonymToken(
        clientId: Int,
        clientSecret: String?,
        v: String?
    ): Flow<AnonymTokenResponse>

    @CheckResult
    fun setAuthCodeStatus(
        authCode: String?,
        apiId: Int,
        accessToken: String?,
        v: String?
    ): Flow<SetAuthCodeStatusResponse>

    @CheckResult
    fun getAuthCodeStatus(
        authCode: String?,
        apiId: Int,
        accessToken: String?,
        v: String?
    ): Flow<GetAuthCodeStatusResponse>
}