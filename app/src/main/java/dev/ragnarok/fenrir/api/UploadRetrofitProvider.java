package dev.ragnarok.fenrir.api;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.BuildConfig;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.settings.IProxySettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class UploadRetrofitProvider implements IUploadRetrofitProvider {

    private final IProxySettings proxySettings;
    private final Object uploadRetrofitLock = new Object();
    private volatile RetrofitWrapper uploadRetrofitInstance;

    public UploadRetrofitProvider(IProxySettings proxySettings) {
        this.proxySettings = proxySettings;
        this.proxySettings.observeActive()
                .subscribe(ignored -> onProxySettingsChanged());
    }

    private void onProxySettingsChanged() {
        synchronized (uploadRetrofitLock) {
            if (nonNull(uploadRetrofitInstance)) {
                uploadRetrofitInstance.cleanup();
                uploadRetrofitInstance = null;
            }
        }
    }

    @Override
    public Single<RetrofitWrapper> provideUploadRetrofit() {
        return Single.fromCallable(() -> {

            if (Objects.isNull(uploadRetrofitInstance)) {
                synchronized (uploadRetrofitLock) {
                    if (Objects.isNull(uploadRetrofitInstance)) {
                        uploadRetrofitInstance = RetrofitWrapper.wrap(createUploadRetrofit(), true);
                    }
                }
            }

            return uploadRetrofitInstance;
        });
    }

    private Retrofit createUploadRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS).addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("X-VK-Android-Client", "new").addHeader("User-Agent", Constants.USER_AGENT(AccountType.BY_TYPE)).build();
                    return chain.proceed(request);
                });

        Gson gson = new GsonBuilder()
                .create();

        ProxyUtil.applyProxyConfig(builder, proxySettings.getActiveProxy());

        return new Retrofit.Builder()
                .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/") // dummy
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(builder.build())
                .build();
    }
}