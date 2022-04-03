package dev.ragnarok.fenrir.api.impl

import com.google.gson.Gson
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.api.*
import dev.ragnarok.fenrir.api.interfaces.IAuthApi
import dev.ragnarok.fenrir.api.model.LoginResponse
import dev.ragnarok.fenrir.api.model.VkApiValidationResponce
import dev.ragnarok.fenrir.api.model.response.BaseResponse
import dev.ragnarok.fenrir.api.services.IAuthService
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.getDeviceId
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.exceptions.Exceptions
import io.reactivex.rxjava3.functions.Function
import retrofit2.HttpException

class AuthApi(private val service: IDirectLoginSeviceProvider) : IAuthApi {
    override fun directLogin(
        grantType: String?, clientId: Int, clientSecret: String?,
        username: String?, pass: String?, v: String?, twoFaSupported: Boolean,
        scope: String?, code: String?, captchaSid: String?, captchaKey: String?, forceSms: Boolean
    ): Single<LoginResponse> {
        return service.provideAuthService()
            .flatMap { service: IAuthService ->
                service
                    .directLogin(
                        grantType,
                        clientId,
                        clientSecret,
                        username,
                        pass,
                        v,
                        if (twoFaSupported) 1 else null,
                        scope,
                        code,
                        captchaSid,
                        captchaKey,
                        if (forceSms) 1 else 0,
                        getDeviceId(
                            provideApplicationContext()
                        ),
                        1
                    )
                    .compose(withHttpErrorHandling())
            }
    }

    override fun validatePhone(
        apiId: Int,
        clientId: Int,
        clientSecret: String?,
        sid: String?,
        v: String?
    ): Single<VkApiValidationResponce> {
        return service.provideAuthService()
            .flatMap { service: IAuthService ->
                service
                    .validatePhone(apiId, clientId, clientSecret, sid, v)
                    .map(extractResponseWithErrorHandling())
            }
    }

    companion object {
        private val BASE_RESPONSE_GSON = Gson()
        fun <T : Any> extractResponseWithErrorHandling(): Function<BaseResponse<T>, T> {
            return Function { response: BaseResponse<T> ->
                if (response.error != null) {
                    throw Exceptions.propagate(ApiException(response.error))
                }
                response.response
            }
        }

        private fun <T : Any> withHttpErrorHandling(): SingleTransformer<T, T> {
            return SingleTransformer { single: Single<T> ->
                single.onErrorResumeNext { throwable: Throwable ->
                    if (throwable is HttpException) {
                        try {
                            val body = throwable.response()?.errorBody()
                            val response = BASE_RESPONSE_GSON.fromJson(
                                body?.string(), LoginResponse::class.java
                            )

                            //{"error":"need_captcha","captcha_sid":"846773809328","captcha_img":"https:\/\/api.vk.com\/captcha.php?sid=846773809328"}
                            if ("need_captcha".equals(response.error, ignoreCase = true)) {
                                return@onErrorResumeNext Single.error(
                                    CaptchaNeedException(
                                        response.captchaSid,
                                        response.captchaImg
                                    )
                                )
                            }
                            if ("need_validation".equals(response.error, ignoreCase = true)) {
                                return@onErrorResumeNext Single.error(
                                    NeedValidationException(
                                        response.validationType,
                                        response.redirect_uri,
                                        response.validation_sid
                                    )
                                )
                            }
                            if (response.error.nonNullNoEmpty()) {
                                return@onErrorResumeNext Single.error(
                                    AuthException(
                                        response.error,
                                        response.errorDescription
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    Single.error(throwable)
                }
            }
        }
    }
}