package dev.ragnarok.fenrir.api;

import io.reactivex.rxjava3.core.Single;


public interface IOtherVkRetrofitProvider {
    Single<RetrofitWrapper> provideAuthRetrofit();

    Single<RetrofitWrapper> provideAuthServiceRetrofit();

    Single<RetrofitWrapper> provideLongpollRetrofit();

    Single<RetrofitWrapper> provideLocalServerRetrofit();
}
