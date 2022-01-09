package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.services.ILocalServerService;
import io.reactivex.rxjava3.core.Single;

public interface ILocalServerServiceProvider {
    Single<ILocalServerService> provideLocalServerService();
}
