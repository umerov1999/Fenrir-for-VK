package dev.ragnarok.fenrir.db

class PeerStateEntity(val peerId: Long) {
    var unreadCount = 0
        private set
    var lastCmId = 0
        private set
    var inReadCmId = 0
        private set
    var outReadCmId = 0
        private set

    fun setUnreadCount(unreadCount: Int): PeerStateEntity {
        this.unreadCount = unreadCount
        return this
    }

    fun setLastCmId(lastCmId: Int): PeerStateEntity {
        this.lastCmId = lastCmId
        return this
    }

    fun setInReadCmId(inReadCmId: Int): PeerStateEntity {
        this.inReadCmId = inReadCmId
        return this
    }

    fun setOutReadCmId(outReadCmId: Int): PeerStateEntity {
        this.outReadCmId = outReadCmId
        return this
    }
}