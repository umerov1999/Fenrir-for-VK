package dev.ragnarok.fenrir.api

import com.google.gson.Gson
import dev.ragnarok.fenrir.model.ProxyConfig
import okhttp3.OkHttpClient

interface IVkMethodHttpClientFactory {
    fun createDefaultVkHttpClient(accountId: Int, gson: Gson, config: ProxyConfig?): OkHttpClient
    fun createCustomVkHttpClient(
        accountId: Int,
        token: String,
        gson: Gson,
        config: ProxyConfig?
    ): OkHttpClient

    fun createServiceVkHttpClient(gson: Gson, config: ProxyConfig?): OkHttpClient
}