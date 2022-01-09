package dev.ragnarok.fenrir.api.interfaces;

import java.util.Map;

import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Single;

public interface IOtherApi {
    Single<Optional<String>> rawRequest(String method, Map<String, String> postParams);
}