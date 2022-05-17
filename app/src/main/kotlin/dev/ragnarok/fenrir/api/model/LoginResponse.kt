package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class LoginResponse {
    // {"error":"need_captcha","captcha_sid":"665120559674","captcha_img":"https:\/\/api.vk.com\/captcha.php?sid=665120559674"}
    @SerializedName("access_token")
    var access_token: String? = null

    @SerializedName("user_id")
    var user_id = 0

    @SerializedName("error")
    var error: String? = null

    @SerializedName("error_description")
    var errorDescription: String? = null

    @SerializedName("captcha_sid")
    var captchaSid: String? = null

    @SerializedName("captcha_img")
    var captchaImg: String? = null

    @SerializedName("validation_type")
    var validationType // 2fa_sms or 2fa_app
            : String? = null

    @SerializedName("redirect_uri")
    var redirect_uri: String? = null

    @SerializedName("phone_mask")
    var phoneMask: String? = null

    @SerializedName("validation_sid")
    var validation_sid: String? = null
}