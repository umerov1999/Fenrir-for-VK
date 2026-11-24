package dev.ragnarok.fenrir.model

import dev.ragnarok.fenrir.nonNullNoEmpty

class TypingMessageOrUploadingInConversation(
    val accountId: Long,
    peerId: Long,
    fromIdsArray: LongArray?,
    val action: Int
) {
    val peerId: Long
    private val fromIds: MutableList<Long> = ArrayList()
    fun getFromIds(): List<Long> {
        return fromIds
    }

    init {
        if (fromIdsArray.nonNullNoEmpty()) {
            for (fromId in fromIdsArray) {
                if (accountId != fromId) {
                    this.fromIds.add(fromId)
                }
            }
        }
        this.peerId = peerId
    }
}