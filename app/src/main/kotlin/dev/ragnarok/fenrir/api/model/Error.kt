package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.orZero
import java.lang.reflect.Type

class Error {
    @SerializedName("error_code")
    var errorCode = 0

    @SerializedName("error_msg")
    var errorMsg: String? = null

    @SerializedName("captcha_sid")
    var captchaSid: String? = null

    @SerializedName("captcha_img")
    var captchaImg: String? = null

    @SerializedName("redirect_uri")
    var redirectUri: String? = null

    @SerializedName("request_params")
    var requestParams: List<Params>? = null

    var type: Type? = null

    fun requests(): HashMap<String, String> {
        val params: HashMap<String, String> = HashMap(requestParams?.size.orZero())
        for (i in requestParams.orEmpty()) {
            if (i.key == "method") {
                continue
            }
            params[i.key ?: "empty"] = i.value ?: "empty"
        }
        return params
    }

    operator fun get(requestKey: String): String {
        for (i in requestParams.orEmpty()) {
            if (requestKey == i.key) {
                return i.value ?: "empty"
            }
        }
        return "empty"
    }
}

class Params {
    @SerializedName("key")
    var key: String? = null

    @SerializedName("value")
    var value: String? = null
}