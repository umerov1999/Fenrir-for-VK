package dev.ragnarok.fenrir.api;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.InetSocketAddress;
import java.net.Proxy;

import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.util.ValidationUtil;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;


public class ProxyUtil {

    public static InetSocketAddress obtainAddress(@NonNull ProxyConfig config) {
        if (ValidationUtil.isValidIpAddress(config.getAddress())) {
            return new InetSocketAddress(config.getAddress(), config.getPort());
        } else {
            return InetSocketAddress.createUnresolved(config.getAddress(), config.getPort());
        }
    }

    public static void applyProxyConfig(OkHttpClient.Builder builder, @Nullable ProxyConfig config) {
        if (nonNull(config)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, obtainAddress(config));

            builder.proxy(proxy);

            if (config.isAuthEnabled()) {
                Authenticator authenticator = (route, response) -> {
                    String credential = Credentials.basic(config.getUser(), config.getPass());
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                };

                builder.proxyAuthenticator(authenticator);
            }
        }
    }
}