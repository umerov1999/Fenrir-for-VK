package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.services.IAuthService;
import io.reactivex.rxjava3.core.Single;

public interface IDirectLoginSeviceProvider {
    Single<IAuthService> provideAuthService();
}