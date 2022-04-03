package dev.ragnarok.fenrir.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class SaveAccount {
    @SerializedName("login")
    public String login;
    @SerializedName("password")
    public String password;
    @SerializedName("two_factor_auth")
    public String two_factor_auth;

    public SaveAccount set(String login, String password, String two_factor_auth) {
        this.login = login;
        this.password = password;
        this.two_factor_auth = two_factor_auth;
        return this;
    }
}
