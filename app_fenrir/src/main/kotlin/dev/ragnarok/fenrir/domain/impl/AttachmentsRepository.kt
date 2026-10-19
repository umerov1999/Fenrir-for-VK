package dev.ragnarok.fenrir.domain.impl

import dev.ragnarok.fenrir.db.AttachmentFor
import dev.ragnarok.fenrir.db.interfaces.IAttachmentsStorage
import dev.ragnarok.fenrir.domain.FetchMode
import dev.ragnarok.fenrir.domain.interfaces.IAttachmentsRepository
import dev.ragnarok.fenrir.domain.interfaces.IAttachmentsRepository.IAddEvent
import dev.ragnarok.fenrir.domain.interfaces.IAttachmentsRepository.IBaseEvent
import dev.ragnarok.fenrir.domain.interfaces.IAttachmentsRepository.IRemoveEvent
import dev.ragnarok.fenrir.domain.interfaces.IOwnersRepository
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.buildAttachmentFromDbo
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.fillOwnerIds
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildDboAttachments
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.VKOwnIds
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.createPublishSubject
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

class AttachmentsRepository(
    private val store: IAttachmentsStorage,
    private val ownersRepository: IOwnersRepository
) : IAttachmentsRepository {
    private val addPublishSubject = createPublishSubject<IAddEvent>()
    private val removePublishSubject = createPublishSubject<IRemoveEvent>()
    override fun remove(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachmentKey: String,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean> {
        return store.remove(
            accountId = accountId,
            attachmentFor = attachmentFor,
            attachmentKey = attachmentKey,
            attachToDbId = attachToDbId,
            attachToElement = attachToElement
        )
            .map {
                val event =
                    RemoveEvent(
                        accountId = accountId,
                        attachmentFor = attachmentFor,
                        attachToDbId = attachToDbId,
                        attachToElement = attachToElement,
                        attachmentKey = attachmentKey,
                    )
                removePublishSubject.emit(event)
                true
            }
    }

    override fun removeAll(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean> {
        return store.removeAll(accountId, attachmentFor, attachToDbId, attachToElement)
    }

    override fun attach(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        models: List<AbsModel>
    ): Flow<Boolean> {
        val entities = buildDboAttachments(models)
        return store.attachToBlobSync(
            cv = null,
            attachmentFor = attachmentFor,
            attachmentsEntities = entities,
            attachToDbId = attachToDbId,
            attachToElement = attachToElement,
            replace = false,
            accountId = accountId
        )
            .map {
                val event =
                    AddEvent(accountId, attachmentFor, attachToDbId, attachToElement, models)
                addPublishSubject.emit(event)
                true
            }
    }

    override fun getAttachments(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<List<AbsModel>?> {
        return store.getAttachmentsFromBlob(accountId, attachmentFor, attachToDbId, attachToElement)
            .flatMapConcat { dbos ->
                if (dbos.isNullOrEmpty()) {
                    toFlow(null)
                } else {
                    val ids = VKOwnIds()
                    for (dbo in dbos) {
                        fillOwnerIds(ids, dbo)
                    }
                    ownersRepository
                        .findBaseOwnersDataAsBundle(accountId, ids.all, FetchMode.ANY)
                        .map {
                            val models: MutableList<AbsModel> = ArrayList(dbos.size)
                            for (dbo in dbos) {
                                models.add(buildAttachmentFromDbo(dbo, it))
                            }
                            models
                        }
                }
            }
    }

    override fun observeAdding(): SharedFlow<IAddEvent> {
        return addPublishSubject
    }

    override fun observeRemoving(): SharedFlow<IRemoveEvent> {
        return removePublishSubject
    }

    private class AddEvent(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        override val attachments: List<AbsModel>
    ) : Event(accountId, attachmentFor, attachToDbId, attachToElement), IAddEvent

    private open class Event(
        override val accountId: Long,
        override val attachmentFor: AttachmentFor,
        override val attachToDbId: Int,
        override val attachToElement: Pair<Long, Int>?
    ) : IBaseEvent

    private class RemoveEvent(
        accountId: Long,
        attachmentFor: AttachmentFor,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        override val attachmentKey: String,
    ) : Event(accountId, attachmentFor, attachToDbId, attachToElement), IRemoveEvent
}
