package dev.ragnarok.fenrir.api;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import dev.ragnarok.fenrir.model.ProxyConfig;
import okhttp3.OkHttpClient;


public interface IVkMethodHttpClientFactory {
    OkHttpClient createDefaultVkHttpClient(int accountId, Gson gson, @Nullable ProxyConfig config);

    OkHttpClient createCustomVkHttpClient(int accountId, String token, Gson gson, @Nullable ProxyConfig config);

    OkHttpClient createServiceVkHttpClient(Gson gson, @Nullable ProxyConfig config);
}