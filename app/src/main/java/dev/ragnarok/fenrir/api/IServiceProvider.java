package dev.ragnarok.fenrir.api;

import io.reactivex.rxjava3.core.Single;

public interface IServiceProvider {
    <T> Single<T> provideService(int accountId, Class<T> serviceClass, int... tokenTypes);
}