package dev.ragnarok.fenrir.api.impl

import android.os.SystemClock
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Constants.DEVICE_COUNTRY_CODE
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.api.IDirectLoginServiceProvider
import dev.ragnarok.fenrir.api.OutOfDateException
import dev.ragnarok.fenrir.api.exceptions.ApiException
import dev.ragnarok.fenrir.api.exceptions.AuthException
import dev.ragnarok.fenrir.api.exceptions.NeedValidationException
import dev.ragnarok.fenrir.api.exceptions.VKIdCaptchaNeedException
import dev.ragnarok.fenrir.api.impl.AbsApi.Companion.RANDOM
import dev.ragnarok.fenrir.api.impl.AbsApi.Companion.lock
import dev.ragnarok.fenrir.api.interfaces.IAuthApi
import dev.ragnarok.fenrir.api.model.Error
import dev.ragnarok.fenrir.api.model.Params
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
import dev.ragnarok.fenrir.api.rest.HttpException
import dev.ragnarok.fenrir.isMsgPack
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.service.ApiErrorCodes
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils.getDeviceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.decodeFromBufferedSource
import kotlinx.serialization.msgpack.MsgPack
import okhttp3.FormBody
import okhttp3.Request
import kotlin.coroutines.cancellation.CancellationException

class AuthApi(private val service: IDirectLoginServiceProvider) : IAuthApi {
    override fun directLogin(
        grantType: String?,
        clientId: Int,
        username: String?,
        password: String?,
        v: String?,
        twoFaSupported: Boolean,
        scope: String?,
        captchaSuccessToken: String?,
        libVerifySupport: Boolean,
        sid: String?,
        anonymousToken: String?,
        sakVersion: String?,
        flowType: String?
    ): Flow<LoginResponse> {
        return service.provideAuthService().flatMapConcat {
            it.directLogin(
                grantType,
                clientId,
                username,
                password,
                v,
                if (twoFaSupported) 1 else null,
                scope,
                captchaSuccessToken,
                getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ),
                if (libVerifySupport) 1 else null,
                DEVICE_COUNTRY_CODE,
                sid,
                anonymousToken,
                sakVersion,
                flowType
            ).map { response ->
                when {
                    "need_captcha".equals(response.error, ignoreCase = true) -> {
                        response.redirect_uri.nonNullNoEmpty { uri ->
                            throw VKIdCaptchaNeedException(
                                uri,
                                "https://" + Settings.get().main().authDomain
                            )
                        }
                        throw AuthException(
                            response.error.orEmpty(), response.errorDescription
                        )
                    }

                    "need_validation".equals(response.error, ignoreCase = true) -> {
                        throw NeedValidationException(
                            username,
                            response.validationType,
                            response.redirect_uri,
                            response.validation_sid,
                            response.errorDescription
                        )
                    }

                    response.error.nonNullNoEmpty() -> {
                        throw AuthException(
                            response.error.orEmpty(), response.errorDescription
                        )
                    }

                    else -> response
                }
            }
        }
    }

    override fun authByExchangeToken(
        clientId: Int,
        exchangeToken: String,
        scope: String,
        initiator: String,
        deviceId: String?,
        sakVersion: String?,
        v: String?
    ): Flow<VKUrlResponse> {
        return service.provideAuthService().flatMapConcat {
            it.authByExchangeToken(
                clientId,
                exchangeToken,
                scope,
                initiator,
                deviceId,
                sakVersion,
                v,
                DEVICE_COUNTRY_CODE
            ).map { s ->
                if (s.error != null) {
                    throw AuthException(s.error.orEmpty(), s.errorDescription)
                } else {
                    s
                }
            }
        }
    }

    override fun validateAccount(
        apiId: Int,
        supportedWays: String?,
        login: String,
        forcePassword: Boolean,
        passkeySupported: Boolean,
        sakVersion: String?,
        flowType: String?,
        accessToken: String?,
        v: String?
    ): Flow<VKApiValidateAccount> {
        return service.provideAuthService().flatMapConcat {
            it.validateAccount(
                apiId,
                DEVICE_COUNTRY_CODE,
                getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ),
                supportedWays,
                login,
                if (forcePassword) 1 else 0,
                if (passkeySupported) 1 else 0,
                sakVersion,
                flowType,
                accessToken,
                v
            ).map(extractResponseWithErrorHandling())
        }
    }

    override fun sendEcosystemOtp(
        apiId: Int,
        sid: String?,
        accessToken: String?,
        v: String?,
        suffix: String
    ): Flow<EcosystemSendOtp> {
        return service.provideAuthService().flatMapConcat {
            it.sendEcosystemOtp(
                apiId,
                DEVICE_COUNTRY_CODE,
                getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ),
                sid,
                accessToken,
                v,
                suffix
            ).map(extractResponseWithErrorHandling())
        }
    }

    override fun checkEcosystemOtp(
        apiId: Int,
        sid: String?,
        accessToken: String?,
        v: String?,
        verificationMethod: String,
        code: String
    ): Flow<EcosystemCheckOtp> {
        return service.provideAuthService().flatMapConcat {
            it.checkEcosystemOtp(
                apiId,
                DEVICE_COUNTRY_CODE,
                getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ),
                sid,
                accessToken,
                v,
                verificationMethod,
                code
            ).map(extractResponseWithErrorHandling())
        }
    }

    override fun getEcosystemVerificationMethods(
        apiId: Int,
        sid: String?,
        accessToken: String?,
        v: String?
    ): Flow<EcosystemGetVerificationMethods> {
        return service.provideAuthService().flatMapConcat {
            it.getEcosystemVerificationMethods(
                apiId,
                DEVICE_COUNTRY_CODE,
                getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ),
                sid,
                accessToken,
                v
            ).map(extractResponseWithErrorHandling())
        }
    }

    override fun getAnonymToken(
        clientId: Int, clientSecret: String?, v: String?
    ): Flow<AnonymTokenResponse> {
        return service.provideAuthService().flatMapConcat {
            it.getAnonymToken(
                clientId, clientSecret, v, getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ), DEVICE_COUNTRY_CODE
            ).map { res ->
                if (res.error != null) {
                    throw AuthException(
                        res.error.orEmpty(), res.errorDescription
                    )
                } else {
                    res
                }
            }
        }
    }

    override fun setAuthCodeStatus(
        authCode: String?, apiId: Int, accessToken: String?, v: String?
    ): Flow<SetAuthCodeStatusResponse> {
        return service.provideAuthService().flatMapConcat {
            it.setAuthCodeStatus(
                authCode, apiId, getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ), accessToken, DEVICE_COUNTRY_CODE, v
            ).map(extractResponseWithErrorHandling())
        }
    }

    override fun getAuthCodeStatus(
        authCode: String?, apiId: Int, accessToken: String?, v: String?
    ): Flow<GetAuthCodeStatusResponse> {
        return service.provideAuthService().flatMapConcat {
            it.getAuthCodeStatus(
                authCode, apiId, getDeviceId(
                    Constants.DEFAULT_ACCOUNT_TYPE, provideApplicationContext()
                ), accessToken, DEVICE_COUNTRY_CODE, v
            ).map(extractResponseWithErrorHandling())
        }
    }

    @Suppress("unchecked_cast")
    private fun <T : Any> rawVKRequest(
        method: String,
        postParams: Map<String, String>,
        serializerType: KSerializer<*>
    ): Flow<BaseResponse<T>> {
        val bodyBuilder = FormBody.Builder()
        for ((key, value) in postParams) {
            bodyBuilder.add(key, value)
        }
        return Includes.networkInterfaces.getVkRestProvider()
            .provideRawHttpClient(Constants.DEFAULT_ACCOUNT_TYPE, null)
            .flatMapConcat { client ->
                flow {
                    val request: Request = Request.Builder()
                        .url(
                            method
                        )
                        .post(bodyBuilder.build())
                        .build()
                    val call = client.build().newCall(request)
                    try {
                        val response = call.execute()
                        if (!response.isSuccessful) {
                            throw HttpException(response.code)
                        } else {
                            emit(response)
                        }
                        response.close()
                    } catch (e: CancellationException) {
                        call.cancel()
                        throw e
                    }
                }
            }
            .map { response ->
                val k = if (response.body.isMsgPack()) MsgPack().decodeFromOkioStream(
                    serializerType, response.body.source()
                ) as BaseResponse<T> else kJson.decodeFromBufferedSource(
                    serializerType, response.body.source()
                ) as BaseResponse<T>
                k.error?.let {
                    it.serializer = serializerType
                    val o = ArrayList<Params>()
                    for ((key, value) in postParams) {
                        val tmp = Params()
                        tmp.key = key
                        tmp.value = value
                        o.add(tmp)
                    }
                    val tmp = Params()
                    tmp.key = "post_url"
                    tmp.value = method
                    o.add(tmp)
                    var authHeader = response.request.header("Authorization")
                    if (authHeader.nonNullNoEmpty()) {
                        authHeader = authHeader.removePrefix("Bearer ")
                        val accessToken = Params()
                        accessToken.key = "access_token"
                        accessToken.value = authHeader
                        o.add(accessToken)
                    }
                    it.requestParams = o
                }
                k
            }
    }

    private fun handleError(error: Error, params: HashMap<String, String>): Boolean {
        var handle = true
        when (error.errorCode) {
            ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND -> {
                synchronized(lock) {
                    SystemClock.sleep((1000 + RANDOM.nextInt(500)).toLong())
                }
            }

            ApiErrorCodes.CAPTCHA_NEED -> {
                val redirectUri = error.redirectUri
                if (redirectUri.nonNullNoEmpty()) {
                    val provider = Includes.vkIdCaptchaProvider
                    provider.requestCaptcha(
                        redirectUri,
                        "https://" + Settings.get().main().apiDomain
                    )
                    var successToken: String? = null
                    while (true) {
                        try {
                            successToken = provider.lookupSuccessToken(redirectUri)
                            if (successToken != null) {
                                break
                            } else {
                                SystemClock.sleep(1000)
                            }
                        } catch (_: OutOfDateException) {
                            break
                        }
                    }
                    if (successToken.nonNullNoEmpty()) {
                        params["success_token"] = successToken
                    } else {
                        handle = false
                    }
                }
            }

            else -> {
                handle = false
            }
        }
        return handle
    }

    private fun <T : Any> extractResponseWithErrorHandling(): suspend (BaseResponse<T>) -> T = {
        val err = it.error
        if (err != null) {
            val params = err.requests()

            if (!handleError(err, params)) {
                throw ApiException(err)
            } else {
                var method = err["post_url"]
                if ("empty" == method) {
                    method = "https://" + Settings.get()
                        .main().apiDomain + "/method/" + err["method"]
                }
                rawVKRequest<T>(
                    method,
                    params,
                    err.serializer ?: throw UnsupportedOperationException()
                ).map(extractResponseWithErrorHandling()).single()
            }
        } else {
            it.response ?: throw NullPointerException("VK return null response")
        }
    }
}
