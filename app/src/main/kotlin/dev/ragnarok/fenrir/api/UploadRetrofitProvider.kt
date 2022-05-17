package dev.ragnarok.fenrir.api

import com.google.gson.GsonBuilder
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Constants.USER_AGENT
import dev.ragnarok.fenrir.api.RetrofitWrapper.Companion.wrap
import dev.ragnarok.fenrir.settings.IProxySettings
import dev.ragnarok.fenrir.settings.Settings
import io.reactivex.rxjava3.core.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class UploadRetrofitProvider(private val proxySettings: IProxySettings) : IUploadRetrofitProvider {
    private val uploadRetrofitLock = Any()

    @Volatile
    private var uploadRetrofitInstance: RetrofitWrapper? = null
    private fun onProxySettingsChanged() {
        synchronized(uploadRetrofitLock) {
            if (uploadRetrofitInstance != null) {
                uploadRetrofitInstance?.cleanup()
                uploadRetrofitInstance = null
            }
        }
    }

    override fun provideUploadRetrofit(): Single<RetrofitWrapper> {
        return Single.fromCallable {
            if (uploadRetrofitInstance == null) {
                synchronized(uploadRetrofitLock) {
                    if (uploadRetrofitInstance == null) {
                        uploadRetrofitInstance = wrap(createUploadRetrofit(), true)
                    }
                }
            }
            uploadRetrofitInstance
        }
    }

    private fun createUploadRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor()
        if (Constants.IS_DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request =
                    chain.request().newBuilder().addHeader("X-VK-Android-Client", "new").addHeader(
                        "User-Agent", USER_AGENT(
                            AccountType.BY_TYPE
                        )
                    ).build()
                chain.proceed(request)
            })
        val gson = GsonBuilder()
            .create()
        ProxyUtil.applyProxyConfig(builder, proxySettings.activeProxy)
        return Retrofit.Builder()
            .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/") // dummy
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(builder.build())
            .build()
    }

    init {
        proxySettings.observeActive()
            .subscribe { onProxySettingsChanged() }
    }
}