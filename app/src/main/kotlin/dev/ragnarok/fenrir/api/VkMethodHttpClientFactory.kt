package dev.ragnarok.fenrir.api

import com.google.gson.Gson
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.BuildConfig
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.model.ProxyConfig
import dev.ragnarok.fenrir.util.CompressDefaultInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class VkMethodHttpClientFactory : IVkMethodHttpClientFactory {
    override fun createDefaultVkHttpClient(
        accountId: Int,
        gson: Gson,
        config: ProxyConfig?
    ): OkHttpClient {
        return createDefaultVkApiOkHttpClient(
            DefaultVkApiInterceptor(
                accountId,
                Constants.API_VERSION,
                gson
            ), config
        )
    }

    override fun createCustomVkHttpClient(
        accountId: Int,
        token: String,
        gson: Gson,
        config: ProxyConfig?
    ): OkHttpClient {
        return createDefaultVkApiOkHttpClient(
            CustomTokenVkApiInterceptor(
                token,
                Constants.API_VERSION,
                gson,
                AccountType.BY_TYPE,
                accountId
            ), config
        )
    }

    override fun createServiceVkHttpClient(gson: Gson, config: ProxyConfig?): OkHttpClient {
        return createDefaultVkApiOkHttpClient(
            CustomTokenVkApiInterceptor(
                BuildConfig.SERVICE_TOKEN,
                Constants.API_VERSION,
                gson,
                Constants.DEFAULT_ACCOUNT_TYPE,
                null
            ), config
        )
    }

    private fun createDefaultVkApiOkHttpClient(
        interceptor: AbsVkApiInterceptor,
        config: ProxyConfig?
    ): OkHttpClient {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .readTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder().addHeader("X-VK-Android-Client", "new")
                    .addHeader("User-Agent", Constants.USER_AGENT(interceptor.type)).build()
                chain.proceed(request)
            }).addInterceptor(CompressDefaultInterceptor)
        ProxyUtil.applyProxyConfig(builder, config)
        HttpLogger.adjust(builder)
        return builder.build()
    }

    override fun createRawVkApiOkHttpClient(
        @AccountType type: Int,
        config: ProxyConfig?
    ): OkHttpClient {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .readTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder().addHeader("X-VK-Android-Client", "new")
                    .addHeader("User-Agent", Constants.USER_AGENT(type)).build()
                chain.proceed(request)
            }).addInterceptor(CompressDefaultInterceptor)
        ProxyUtil.applyProxyConfig(builder, config)
        HttpLogger.adjust(builder)
        return builder.build()
    }
}