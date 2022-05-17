package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class Error {
    @SerializedName("error_code")
    var errorCode = 0

    @SerializedName("error_msg")
    var errorMsg: String? = null

    @SerializedName("method")
    var method: String? = null

    @SerializedName("captcha_sid")
    var captchaSid: String? = null

    @SerializedName("captcha_img")
    var captchaImg: String? = null

    @SerializedName("redirect_uri")
    var redirectUri: String? = null
}