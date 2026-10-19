package dev.ragnarok.fenrir.domain.interfaces

import dev.ragnarok.fenrir.model.SectionCounters
import kotlinx.coroutines.flow.Flow

interface ICountersInteractor {
    fun getApiCounters(accountId: Long): Flow<SectionCounters>
}