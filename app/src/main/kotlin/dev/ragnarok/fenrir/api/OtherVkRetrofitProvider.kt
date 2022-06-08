package dev.ragnarok.fenrir.api

import android.annotation.SuppressLint
import com.google.gson.GsonBuilder
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Constants.USER_AGENT
import dev.ragnarok.fenrir.api.RetrofitWrapper.Companion.wrap
import dev.ragnarok.fenrir.api.adapters.LongpollUpdateAdapter
import dev.ragnarok.fenrir.api.adapters.LongpollUpdatesAdapter
import dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent
import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.IProxySettings
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.CompressDefaultInterceptor
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.retrofit.gson.GsonConverterFactory
import dev.ragnarok.fenrir.util.retrofit.rxjava3.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Single
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class OtherVkRetrofitProvider @SuppressLint("CheckResult") constructor(private val proxySettings: IProxySettings) :
    IOtherVkRetrofitProvider {
    private val longpollRetrofitLock = Any()
    private val localServerRetrofitLock = Any()
    private var longpollRetrofitInstance: RetrofitWrapper? = null
    private var localServerRetrofitInstance: RetrofitWrapper? = null
    private fun onProxySettingsChanged() {
        synchronized(longpollRetrofitLock) {
            if (longpollRetrofitInstance != null) {
                longpollRetrofitInstance?.cleanup()
                longpollRetrofitInstance = null
            }
        }
        synchronized(localServerRetrofitLock) {
            if (localServerRetrofitInstance != null) {
                localServerRetrofitInstance?.cleanup()
                localServerRetrofitInstance = null
            }
        }
    }

    override fun provideAuthRetrofit(): Single<RetrofitWrapper> {
        return Single.fromCallable {
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val request =
                        chain.request().newBuilder().addHeader("X-VK-Android-Client", "new")
                            .addHeader(
                                "User-Agent", USER_AGENT(
                                    Constants.DEFAULT_ACCOUNT_TYPE
                                )
                            ).build()
                    chain.proceed(request)
                })
                .addInterceptor(CompressDefaultInterceptor)
            ProxyUtil.applyProxyConfig(builder, proxySettings.activeProxy)
            HttpLogger.adjust(builder)
            val gson = GsonBuilder().create()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://" + Settings.get().other().get_Auth_Domain() + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(builder.build())
                .build()
            wrap(retrofit, false)
        }
    }

    override fun provideAuthServiceRetrofit(): Single<RetrofitWrapper> {
        return Single.fromCallable {
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val request =
                        chain.request().newBuilder().addHeader("X-VK-Android-Client", "new")
                            .addHeader(
                                "User-Agent", USER_AGENT(
                                    AccountType.BY_TYPE
                                )
                            ).build()
                    chain.proceed(request)
                })
                .addInterceptor(CompressDefaultInterceptor)
            ProxyUtil.applyProxyConfig(builder, proxySettings.activeProxy)
            HttpLogger.adjust(builder)
            val gson = GsonBuilder().create()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(builder.build())
                .build()
            wrap(retrofit, false)
        }
    }

    private fun createLocalServerRetrofit(): Retrofit {
        val localSettings = Settings.get().other().localServer
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request =
                    chain.request().newBuilder().addHeader("X-VK-Android-Client", "new").addHeader(
                        "User-Agent", USER_AGENT(
                            AccountType.BY_TYPE
                        )
                    ).build()
                chain.proceed(request)
            }).addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original = chain.request()
                val formBuilder = FormBody.Builder()
                val body = original.body
                if (body is FormBody) {
                    for (i in 0 until body.size) {
                        formBuilder.add(body.name(i), body.value(i))
                    }
                }
                localSettings.password.nonNullNoEmpty {
                    formBuilder.add("password", it)
                }
                val request = original.newBuilder()
                    .method("POST", formBuilder.build())
                    .build()
                chain.proceed(request)
            }).addInterceptor(CompressDefaultInterceptor)
        HttpLogger.adjust(builder)
        val url = Utils.firstNonEmptyString(localSettings.url, "https://debug.dev")!!
        return Retrofit.Builder()
            .baseUrl("$url/method/")
            .addConverterFactory(GsonConverterFactory.create(VkRetrofitProvider.vkgson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(builder.build())
            .build()
    }

    private fun createLongpollRetrofitInstance(): Retrofit {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request =
                    chain.request().newBuilder().addHeader("X-VK-Android-Client", "new").addHeader(
                        "User-Agent", USER_AGENT(
                            AccountType.BY_TYPE
                        )
                    ).build()
                chain.proceed(request)
            })
            .addInterceptor(CompressDefaultInterceptor)
        ProxyUtil.applyProxyConfig(builder, proxySettings.activeProxy)
        HttpLogger.adjust(builder)
        val gson = GsonBuilder()
            .registerTypeAdapter(VkApiLongpollUpdates::class.java, LongpollUpdatesAdapter())
            .registerTypeAdapter(AbsLongpollEvent::class.java, LongpollUpdateAdapter())
            .create()
        return Retrofit.Builder()
            .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/") // dummy
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(builder.build())
            .build()
    }

    override fun provideLocalServerRetrofit(): Single<RetrofitWrapper> {
        return Single.fromCallable {
            if (localServerRetrofitInstance == null) {
                synchronized(localServerRetrofitLock) {
                    if (localServerRetrofitInstance == null) {
                        localServerRetrofitInstance = wrap(createLocalServerRetrofit())
                    }
                }
            }
            localServerRetrofitInstance
        }
    }

    override fun provideLongpollRetrofit(): Single<RetrofitWrapper> {
        return Single.fromCallable {
            if (longpollRetrofitInstance == null) {
                synchronized(longpollRetrofitLock) {
                    if (longpollRetrofitInstance == null) {
                        longpollRetrofitInstance = wrap(createLongpollRetrofitInstance())
                    }
                }
            }
            longpollRetrofitInstance
        }
    }

    init {
        proxySettings.observeActive()
            .subscribe { onProxySettingsChanged() }
    }
}