package dev.ragnarok.fenrir.domain

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.db.AttachToType
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.util.Pair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface IAttachmentsRepository {
    @CheckResult
    fun remove(
        accountId: Long,
        @AttachToType type: Int,
        attachToDbId: Int,
        generatedAttachmentId: Int
    ): Flow<Boolean>

    @CheckResult
    fun removeAll(
        accountId: Long,
        @AttachToType type: Int,
        attachToDbId: Int
    ): Flow<Boolean>

    @CheckResult
    fun attach(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        models: List<AbsModel>
    ): Flow<Boolean>

    fun getAttachmentsWithIds(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int
    ): Flow<List<Pair<Int, AbsModel>>>

    fun observeAdding(): SharedFlow<IAddEvent>
    fun observeRemoving(): SharedFlow<IRemoveEvent>
    interface IBaseEvent {
        val accountId: Long

        @get:AttachToType
        val attachToType: Int
        val attachToDbId: Int
    }

    interface IRemoveEvent : IBaseEvent {
        val generatedId: Int
    }

    interface IAddEvent : IBaseEvent {
        val attachments: List<Pair<Int, AbsModel>>
    }
}
