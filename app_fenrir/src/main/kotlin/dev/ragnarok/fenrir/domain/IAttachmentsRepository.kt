package dev.ragnarok.fenrir.domain

import android.content.ContentValues
import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.db.AttachToType
import dev.ragnarok.fenrir.db.model.entity.DboEntity
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.util.Pair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface IAttachmentsRepository {
    @CheckResult
    fun remove(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachmentKey: String,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean>

    @CheckResult
    fun removeAll(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean>

    @CheckResult
    fun attach(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        models: List<AbsModel>
    ): Flow<Boolean>

    @CheckResult
    fun getAttachments(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<List<AbsModel>?>

    fun observeAdding(): SharedFlow<IAddEvent>
    fun observeRemoving(): SharedFlow<IRemoveEvent>
    interface IBaseEvent {
        val accountId: Long

        @get:AttachToType
        val attachToType: Int
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
