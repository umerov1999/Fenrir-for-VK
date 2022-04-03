package dev.ragnarok.fenrir.api

import dev.ragnarok.fenrir.model.ProxyConfig
import dev.ragnarok.fenrir.util.ValidationUtil
import okhttp3.Authenticator
import okhttp3.Credentials.basic
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
import java.net.InetSocketAddress
import java.net.Proxy

object ProxyUtil {
    private fun obtainAddress(config: ProxyConfig): InetSocketAddress {
        return if (ValidationUtil.isValidIpAddress(config.address)) {
            InetSocketAddress(config.address, config.port)
        } else {
            InetSocketAddress.createUnresolved(config.address, config.port)
        }
    }

    @JvmStatic
    fun applyProxyConfig(builder: OkHttpClient.Builder, config: ProxyConfig?) {
        if (config != null) {
            val proxy = Proxy(
                Proxy.Type.HTTP, obtainAddress(
                    config
                )
            )
            builder.proxy(proxy)
            if (config.isAuthEnabled) {
                val authenticator = Authenticator { _: Route?, response: Response ->
                    val credential = basic(
                        config.user, config.pass
                    )
                    response.request.newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build()
                }
                builder.proxyAuthenticator(authenticator)
            }
        }
    }
}