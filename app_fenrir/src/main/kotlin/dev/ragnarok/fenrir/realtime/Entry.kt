package dev.ragnarok.fenrir.realtime

import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate
import dev.ragnarok.fenrir.longpoll.FullAndNonFullUpdates
import dev.ragnarok.fenrir.util.Utils.safeCountOf

class Entry(val accountId: Long, val id: Int, val isIgnoreIfExists: Boolean) {
    val updates: FullAndNonFullUpdates = FullAndNonFullUpdates()
    fun has(id: Int, peerId: Long): Boolean {
        if (updates.hasNonFullMessages()) {
            for (nonFullId in updates.nonFull.orEmpty()) {
                if (id == nonFullId.messageId && peerId == nonFullId.peerId) {
                    return true
                }
            }
        }
        if (updates.hasFullMessages()) {
            for (update in updates.fullMessages.orEmpty()) {
                if (update.messageId == id && peerId == update.peerId) {
                    return true
                }
            }
        }
        return false
    }

    fun count(): Int {
        return safeCountOf(updates.fullMessages) + safeCountOf(updates.nonFull)
    }

    fun append(update: AddMessageUpdate) {
        if (update.isFull) {
            updates.prepareFull().add(update)
        } else {
            updates.prepareNonFull().add(update)
        }
    }

    fun append(messageId: Int, peerId: Long) {
        val u = AddMessageUpdate()
        u.messageId = messageId
        u.peerId = peerId
        updates.prepareNonFull().add(u)
    }

}
