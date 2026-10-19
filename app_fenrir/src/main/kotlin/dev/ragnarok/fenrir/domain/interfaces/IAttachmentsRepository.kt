package dev.ragnarok.fenrir.domain.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.db.AttachmentFor
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.util.Pair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface IAttachmentsRepository {
    @CheckResult
    fun remove(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachmentKey: String,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean>

    @CheckResult
    fun removeAll(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean>

    @CheckResult
    fun attach(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        models: List<AbsModel>
    ): Flow<Boolean>

    @CheckResult
    fun getAttachments(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<List<AbsModel>?>

    fun observeAdding(): SharedFlow<IAddEvent>
    fun observeRemoving(): SharedFlow<IRemoveEvent>
    interface IBaseEvent {
        val accountId: Long

        val attachmentFor: AttachmentFor
        val attachToDbId: Int

        val attachToElement: Pair<Long, Int>?
    }

    interface IRemoveEvent : IBaseEvent {
        val attachmentKey: String
    }

    interface IAddEvent : IBaseEvent {
        val attachments: List<AbsModel>
    }
}
