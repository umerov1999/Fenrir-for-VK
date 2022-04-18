package dev.ragnarok.fenrir.api

import android.os.SystemClock
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.captchaProvider
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.Includes.validationProvider
import dev.ragnarok.fenrir.api.model.Captcha
import dev.ragnarok.fenrir.api.model.response.VkReponse
import dev.ragnarok.fenrir.exception.UnauthorizedException
import dev.ragnarok.fenrir.service.ApiErrorCodes
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.PersistentLogger.logThrowable
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.refresh.RefreshToken
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.*

abstract class AbsVkApiInterceptor(private val version: String, private val gson: Gson) :
    Interceptor {
    protected abstract val token: String?

    @AccountType
    abstract val type: Int
    protected abstract val accountId: Int

    /*
   private String RECEIPT_GMS_TOKEN() {
       try {
           GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
           int isGooglePlayServicesAvailable = instance.isGooglePlayServicesAvailable(Includes.provideApplicationContext());
           if (isGooglePlayServicesAvailable != 0) {
               return null;
           }
           return FirebaseInstanceId.getInstance().getToken("54740537194", "id" + getAccountId());
       } catch (Throwable th) {
           th.printStackTrace();
           return null;
       }
   }
    */

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var vkAccessToken: String = if (token.isNullOrEmpty()) {
            throw UnauthorizedException("No authorization! Please, login and retry")
        } else {
            token!!
        }
        val original: Request = chain.request()

        val formBuilder = FormBody.Builder()
        val body = original.body
        var HasVersion = false
        var HasDeviceId = false
        if (body is FormBody) {
            for (i in 0 until body.size) {
                val name = body.name(i)
                if (name == "v") {
                    HasVersion = true
                } else if (name == "device_id") HasDeviceId = true
                val value = body.value(i)
                formBuilder.add(name, value)
            }
        }
        if (!HasVersion) formBuilder.add("v", version)
        formBuilder.add("access_token", vkAccessToken)
            .add("lang", Constants.DEVICE_COUNTRY_CODE)
            .add("https", "1")
        if (!HasDeviceId) formBuilder.add(
            "device_id",
            Utils.getDeviceId(provideApplicationContext())
        )
        var request = original.newBuilder()
            .method("POST", formBuilder.build())
            .build()
        var response: Response
        var responseBody: ResponseBody?
        var responseBodyString: String
        while (true) {
            response = chain.proceed(request)
            responseBody = response.body
            assert(responseBody != null)
            responseBodyString = responseBody!!.string()
            var vkReponse: VkReponse
            try {
                vkReponse = gson.fromJson(responseBodyString, VkReponse::class.java)
            } catch (ignored: JsonSyntaxException) {
                responseBodyString =
                    "{ \"error\": { \"error_code\": -1, \"error_msg\": \"Internal json syntax error\" } }"
                return response.newBuilder()
                    .body(responseBodyString.toResponseBody(responseBody.contentType()))
                    .build()
            }
            val error = vkReponse?.error
            if (error != null) {
                when (error.errorCode) {
                    ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND -> {}
                    ApiErrorCodes.CAPTCHA_NEED -> if (Settings.get().other().isExtra_debug) {
                        logThrowable(
                            "Captcha request",
                            Exception("URL: " + request.url + ", dump: " + Gson().toJson(error))
                        )
                    }
                    else -> if (Settings.get().other().isExtra_debug) {
                        val uu = StringBuilder()
                        val formBody: FormBody = formBuilder.build()
                        var first = true
                        var i = 0
                        while (i < formBody.size) {
                            val name = formBody.name(i)
                            val value = formBody.value(i)
                            if (first) {
                                first = false
                            } else {
                                uu.append("; ")
                            }
                            uu.append(name).append("=").append(value)
                            i++
                        }
                        logThrowable(
                            "ApiError",
                            Exception("Method: " + original.url + ", code: " + error.errorCode + ", message: " + error.errorMsg + ", params: { " + uu + " }.")
                        )
                    }
                }
                if (error.errorCode == ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND) {
                    synchronized(AbsVkApiInterceptor::class.java) {
                        val sleepMs = 1000 + RANDOM.nextInt(500)
                        SystemClock.sleep(sleepMs.toLong())
                    }
                    continue
                }
                if (error.errorCode == ApiErrorCodes.REFRESH_TOKEN || error.errorCode == ApiErrorCodes.CLIENT_VERSION_DEPRECATED) {
                    if (RefreshToken.upgradeToken(accountId, vkAccessToken)) {
                        vkAccessToken = token ?: continue
                        formBuilder.add("access_token", vkAccessToken)
                        request = original.newBuilder()
                            .method("POST", formBuilder.build())
                            .build()
                        continue
                    }
                }
                if (error.errorCode == ApiErrorCodes.VALIDATE_NEED) {
                    val provider = validationProvider
                    provider.requestValidate(error.redirectUri)
                    var code = false
                    while (true) {
                        try {
                            code = provider.lookupState(error.redirectUri ?: break)
                            if (code) {
                                break
                            } else {
                                SystemClock.sleep(1000)
                            }
                        } catch (e: OutOfDateException) {
                            break
                        }
                    }
                    if (code) {
                        vkAccessToken =
                            Settings.get().accounts().getAccessToken(accountId) ?: continue
                        formBuilder.add("access_token", vkAccessToken)
                        request = original.newBuilder()
                            .method("POST", formBuilder.build())
                            .build()
                        continue
                    }
                }
                if (error.errorCode == ApiErrorCodes.CAPTCHA_NEED) {
                    val captcha = Captcha(error.captchaSid, error.captchaImg)
                    val provider = captchaProvider
                    provider.requestCaptha(captcha.sid, captcha)
                    var code: String? = null
                    while (true) {
                        try {
                            code = provider.lookupCode(captcha.sid ?: break)
                            if (code != null) {
                                break
                            } else {
                                SystemClock.sleep(1000)
                            }
                        } catch (e: OutOfDateException) {
                            break
                        }
                    }
                    if (Settings.get().other().isExtra_debug) {
                        logThrowable(
                            "Captcha answer",
                            Exception("URL: " + request.url + ", code: " + code + ", sid: " + captcha.sid)
                        )
                    }
                    if (code != null && captcha.sid != null) {
                        formBuilder.add("captcha_sid", captcha.sid ?: continue)
                        formBuilder.add("captcha_key", code)
                        request = original.newBuilder()
                            .method("POST", formBuilder.build())
                            .build()
                        continue
                    }
                }
            }
            break
        }
        return response.newBuilder()
            .body(responseBodyString.toResponseBody(responseBody?.contentType())).build()
    }

    companion object {
        private val RANDOM = Random()
    }
}