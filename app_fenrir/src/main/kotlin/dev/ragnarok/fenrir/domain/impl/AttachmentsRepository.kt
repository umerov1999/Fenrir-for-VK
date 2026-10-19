package dev.ragnarok.fenrir.domain.impl

import dev.ragnarok.fenrir.db.AttachToType
import dev.ragnarok.fenrir.db.interfaces.IAttachmentsStorage
import dev.ragnarok.fenrir.domain.IAttachmentsRepository
import dev.ragnarok.fenrir.domain.IAttachmentsRepository.IAddEvent
import dev.ragnarok.fenrir.domain.IAttachmentsRepository.IBaseEvent
import dev.ragnarok.fenrir.domain.IAttachmentsRepository.IRemoveEvent
import dev.ragnarok.fenrir.domain.IOwnersRepository
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
        @AttachToType attachToType: Int,
        attachmentKey: String,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean> {
        return store.remove(
            accountId = accountId,
            attachToType = attachToType,
            attachmentKey = attachmentKey,
            attachToDbId = attachToDbId,
            attachToElement = attachToElement
        )
            .map {
                val event =
                    RemoveEvent(
                        accountId = accountId,
                        attachToType = attachToType,
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
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<Boolean> {
        return store.removeAll(accountId, attachToType, attachToDbId, attachToElement)
    }

    override fun attach(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        models: List<AbsModel>
    ): Flow<Boolean> {
        val entities = buildDboAttachments(models)
        return store.attachToBlobSync(
            cv = null,
            attachToType = attachToType,
            attachmentsEntities = entities,
            attachToDbId = attachToDbId,
            attachToElement = attachToElement,
            replace = false,
            accountId = accountId
        )
            .map {
                val event = AddEvent(accountId, attachToType, attachToDbId, attachToElement, models)
                addPublishSubject.emit(event)
                true
            }
    }

    override fun getAttachments(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?
    ): Flow<List<AbsModel>?> {
        return store.getAttachmentsFromBlob(accountId, attachToType, attachToDbId, attachToElement)
            .flatMapConcat { dbos ->
                if (dbos.isNullOrEmpty()) {
                    toFlow(null)
                } else {
                    val ids = VKOwnIds()
                    for (dbo in dbos) {
                        fillOwnerIds(ids, dbo)
                    }
                    ownersRepository
                        .findBaseOwnersDataAsBundle(accountId, ids.all, IOwnersRepository.MODE_ANY)
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
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        override val attachments: List<AbsModel>
    ) : Event(accountId, attachToType, attachToDbId, attachToElement), IAddEvent

    private open class Event(
        override val accountId: Long,
        @param:AttachToType override val attachToType: Int,
        override val attachToDbId: Int,
        override val attachToElement: Pair<Long, Int>?
    ) : IBaseEvent

    private class RemoveEvent(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        attachToElement: Pair<Long, Int>?,
        override val attachmentKey: String,
    ) : Event(accountId, attachToType, attachToDbId, attachToElement), IRemoveEvent
}
