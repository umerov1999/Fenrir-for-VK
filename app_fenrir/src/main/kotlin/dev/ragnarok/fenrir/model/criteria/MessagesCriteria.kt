package dev.ragnarok.fenrir.model.criteria

import dev.ragnarok.fenrir.orZero

class MessagesCriteria(val accountId: Long, val peerId: Long) : Criteria() {
    var startCmId: Int? = null
        private set

    override fun toString(): String {
        return "MessagesCriteria{" +
                "peerId=" + peerId +
                ", startCmId=" + startCmId +
                "} " + super.toString()
    }

    fun setStartCmId(startCmId: Int?): MessagesCriteria {
        this.startCmId = startCmId
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is MessagesCriteria && accountId == other.accountId && peerId == other.peerId && startCmId == other.startCmId
    }

    override fun hashCode(): Int {
        var result = accountId.hashCode()
        result = 31 * result + peerId.hashCode()
        result = 31 * result + startCmId?.hashCode().orZero()
        return result
    }
}