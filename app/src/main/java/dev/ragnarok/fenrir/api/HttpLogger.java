package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.Constants;
import okhttp3.logging.HttpLoggingInterceptor;


public class HttpLogger {

    public static final HttpLoggingInterceptor DEFAULT_LOGGING_INTERCEPTOR = new HttpLoggingInterceptor();

    static {
        if (Constants.IS_DEBUG) {
            DEFAULT_LOGGING_INTERCEPTOR.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            DEFAULT_LOGGING_INTERCEPTOR.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
    }
}