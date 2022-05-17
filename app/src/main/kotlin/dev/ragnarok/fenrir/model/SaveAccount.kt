package dev.ragnarok.fenrir.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class SaveAccount {
    @SerializedName("login")
    var login: String? = null

    @SerializedName("password")
    var password: String? = null

    @SerializedName("two_factor_auth")
    var two_factor_auth: String? = null
    operator fun set(login: String?, password: String?, two_factor_auth: String?): SaveAccount {
        this.login = login
        this.password = password
        this.two_factor_auth = two_factor_auth
        return this
    }
}