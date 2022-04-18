package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;


public class LoginResponse {

    // {"error":"need_captcha","captcha_sid":"665120559674","captcha_img":"https:\/\/api.vk.com\/captcha.php?sid=665120559674"}

    @Nullable
    @SerializedName("access_token")
    public String access_token;

    @SerializedName("user_id")
    public int user_id;

    @Nullable
    @SerializedName("error")
    public String error;

    @Nullable
    @SerializedName("error_description")
    public String errorDescription;

    @Nullable
    @SerializedName("captcha_sid")
    public String captchaSid;

    @Nullable
    @SerializedName("captcha_img")
    public String captchaImg;

    @Nullable
    @SerializedName("validation_type")
    public String validationType; // 2fa_sms or 2fa_app

    @Nullable
    @SerializedName("redirect_uri")
    public String redirect_uri;

    @Nullable
    @SerializedName("phone_mask")
    public String phoneMask;

    @Nullable
    @SerializedName("validation_sid")
    public String validation_sid;
}