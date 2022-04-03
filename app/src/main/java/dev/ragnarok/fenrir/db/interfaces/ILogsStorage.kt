package dev.ragnarok.fenrir.db.interfaces

import dev.ragnarok.fenrir.model.LogEvent
import io.reactivex.rxjava3.core.Single

interface ILogsStorage {
    fun add(type: Int, tag: String, body: String): Single<LogEvent>
    fun getAll(type: Int): Single<List<LogEvent>>
    fun Clear()
}