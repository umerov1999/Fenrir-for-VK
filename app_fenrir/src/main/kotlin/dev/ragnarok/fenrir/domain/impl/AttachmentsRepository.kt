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
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.VKOwnIds
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.createPublishSubject
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
        @AttachToType type: Int,
        attachToDbId: Int,
        generatedAttachmentId: Int
    ): Flow<Boolean> {
        return store.remove(accountId, type, generatedAttachmentId)
            .map {
                val event =
                    RemoveEvent(accountId, type, attachToDbId, generatedAttachmentId)
                removePublishSubject.emit(event)
                true
            }
    }

    override fun removeAll(
        accountId: Long,
        @AttachToType type: Int,
        attachToDbId: Int
    ): Flow<Boolean> {
        return store.removeAll(accountId, type, attachToDbId)
    }

    override fun attach(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        models: List<AbsModel>
    ): Flow<Boolean> {
        val entities = buildDboAttachments(models)
        return store.attachDbos(accountId, attachToType, attachToDbId, entities)
            .map { ids ->
                val events: MutableList<Pair<Int, AbsModel>> = ArrayList(models.size)
                for (i in models.indices) {
                    val model = models[i]
                    val generatedId = ids[i]
                    events.add(create(generatedId, model))
                }
                val event = AddEvent(accountId, attachToType, attachToDbId, events)
                addPublishSubject.emit(event)
                true
            }
    }

    override fun getAttachmentsWithIds(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int
    ): Flow<List<Pair<Int, AbsModel>>> {
        return store.getAttachmentsDbosWithIds(accountId, attachToType, attachToDbId)
            .flatMapConcat { pairs ->
                val ids = VKOwnIds()
                for (pair in pairs) {
                    fillOwnerIds(ids, pair.second)
                }
                ownersRepository
                    .findBaseOwnersDataAsBundle(accountId, ids.all, IOwnersRepository.MODE_ANY)
                    .map {
                        val models: MutableList<Pair<Int, AbsModel>> = ArrayList(pairs.size)
                        for (pair in pairs) {
                            val model = buildAttachmentFromDbo(pair.second, it)
                            models.add(create(pair.first, model))
                        }
                        models
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
        override val attachments: List<Pair<Int, AbsModel>>
    ) : Event(accountId, attachToType, attachToDbId), IAddEvent

    private open class Event(
        override val accountId: Long,
        @param:AttachToType override val attachToType: Int,
        override val attachToDbId: Int
    ) : IBaseEvent

    private inner class RemoveEvent(
        accountId: Long,
        @AttachToType attachToType: Int,
        attachToDbId: Int,
        override val generatedId: Int
    ) : Event(accountId, attachToType, attachToDbId), IRemoveEvent

}
