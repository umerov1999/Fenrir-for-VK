package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;


public class Error {

    @SerializedName("error_code")
    public int errorCode;

    @Nullable
    @SerializedName("error_msg")
    public String errorMsg;

    @Nullable
    @SerializedName("method")
    public String method;

    @Nullable
    @SerializedName("captcha_sid")
    public String captchaSid;

    @Nullable
    @SerializedName("captcha_img")
    public String captchaImg;

    @Nullable
    @SerializedName("redirect_uri")
    public String redirectUri;
}
