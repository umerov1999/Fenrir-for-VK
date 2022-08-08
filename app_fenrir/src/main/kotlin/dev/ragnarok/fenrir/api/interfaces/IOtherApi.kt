package dev.ragnarok.fenrir.api.interfaces

import dev.ragnarok.fenrir.util.Optional
import io.reactivex.rxjava3.core.Single
import okio.BufferedSource

interface IOtherApi {
    fun rawRequestJson(method: String, postParams: Map<String, String>): Single<Optional<String>>
    fun rawRequestMsgPack(
        method: String,
        postParams: Map<String, String>
    ): Single<Optional<BufferedSource>>
}
