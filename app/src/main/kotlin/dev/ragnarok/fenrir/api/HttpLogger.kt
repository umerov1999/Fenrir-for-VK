package dev.ragnarok.fenrir.api

import dev.ragnarok.fenrir.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object HttpLogger {
    val DEFAULT_LOGGING_INTERCEPTOR: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().setLevel(if (Constants.IS_DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    val UPLOAD_LOGGING_INTERCEPTOR: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().setLevel(if (Constants.IS_DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE)
    }

    fun adjust(builder: OkHttpClient.Builder) {
        if (Constants.IS_DEBUG) {
            builder.addInterceptor(DEFAULT_LOGGING_INTERCEPTOR)
        }
    }

    fun adjustUpload(builder: OkHttpClient.Builder) {
        if (Constants.IS_DEBUG) {
            builder.addInterceptor(UPLOAD_LOGGING_INTERCEPTOR)
        }
    }
}