package dev.ragnarok.fenrir.api

import com.google.gson.Gson
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.settings.Settings

class DefaultVkApiInterceptor internal constructor(
    override val accountId: Int,
    v: String,
    gson: Gson
) : AbsVkApiInterceptor(
    v, gson
) {
    override val token: String?
        get() = Settings.get()
            .accounts()
            .getAccessToken(accountId)

    @AccountType
    override val type: Int
        get() = Settings.get()
            .accounts()
            .getType(accountId)
}