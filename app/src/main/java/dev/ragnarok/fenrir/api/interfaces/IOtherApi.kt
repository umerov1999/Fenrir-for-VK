package dev.ragnarok.fenrir.api.interfaces

import dev.ragnarok.fenrir.util.Optional
import io.reactivex.rxjava3.core.Single

interface IOtherApi {
    fun rawRequest(method: String, postParams: Map<String, String>): Single<Optional<String>>
}