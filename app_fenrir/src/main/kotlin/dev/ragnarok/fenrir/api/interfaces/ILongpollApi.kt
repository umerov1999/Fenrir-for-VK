package dev.ragnarok.fenrir.api.interfaces

import dev.ragnarok.fenrir.api.model.longpoll.VKApiLongpollUpdates
import kotlinx.coroutines.flow.Flow

interface ILongpollApi {
    fun getUpdates(
        server: String,
        key: String?,
        ts: Long,
        wait: Long,
        mode: Int,
        version: Int
    ): Flow<VKApiLongpollUpdates>
}