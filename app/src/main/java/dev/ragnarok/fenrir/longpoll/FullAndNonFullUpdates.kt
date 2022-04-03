package dev.ragnarok.fenrir.longpoll

import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate
import dev.ragnarok.fenrir.nonNullNoEmpty

class FullAndNonFullUpdates {
    var fullMessages: MutableList<AddMessageUpdate>? = null
        private set
    var nonFull: MutableList<AddMessageUpdate>? = null
        private set

    fun prepareFull(): MutableList<AddMessageUpdate> {
        if (fullMessages == null) {
            fullMessages = ArrayList(1)
        }
        return fullMessages!!
    }

    fun prepareNonFull(): MutableList<AddMessageUpdate> {
        if (nonFull == null) {
            nonFull = ArrayList(1)
        }
        return nonFull!!
    }

    fun hasFullMessages(): Boolean {
        return fullMessages.nonNullNoEmpty()
    }

    fun hasNonFullMessages(): Boolean {
        return nonFull.nonNullNoEmpty()
    }

    override fun toString(): String {
        return "FullAndNonFullUpdates[" +
                "full=" + (if (fullMessages == null) 0 else fullMessages!!.size) +
                ", nonFull=" + (if (nonFull == null) 0 else nonFull!!.size) + ']'
    }
}