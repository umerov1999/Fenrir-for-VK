package dev.ragnarok.fenrir.realtime

import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.interfaces.Identificable
import dev.ragnarok.fenrir.api.model.interfaces.IdentificablePair
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.util.Pair

class TmpResult(val id: Int, val accountId: Long, capacity: Int) {
    val data: MutableList<Msg> = ArrayList(capacity)
    fun prepare(id: Int, peerId: Long): Msg {
        for (m in data) {
            if (m.id == id && m.peerId == peerId) {
                return m
            }
        }
        return add(id, peerId)
    }

    fun appendDtos(dtos: List<VKApiMessage>): TmpResult {
        for (dto in dtos) {
            prepare(dto.id, dto.peer_id).setDto(dto)
        }
        return this
    }

    fun appendModel(messages: List<Message>): TmpResult {
        for (m in messages) {
            prepare(m.getObjectId(), m.peerId).setMessage(m)
        }
        return this
    }

    fun collectDtos(): List<VKApiMessage> {
        val dtos: MutableList<VKApiMessage> = ArrayList(data.size)
        for (msg in data) {
            msg.dto?.let { dtos.add(it) }
        }
        return dtos
    }

    fun add(id: Int, peerId: Long): Msg {
        val msg = Msg(id, peerId)
        data.add(msg)
        return msg
    }

    fun setMissingIds(ids: Collection<Pair<Long, Int>>): TmpResult {
        for (msg in data) {
            var has = false
            for(tId in ids) {
                if (tId.first == msg.peerId && tId.second == msg.id) {
                    has = true
                    break
                }
            }
            msg.setAlreadyExists(has)
        }
        return this
    }

    val allIds: List<Pair<Long, Int>>
        get() {
            if (data.isEmpty()) {
                return emptyList()
            }
            if (data.size == 1) {
                return listOf(Pair(data[0].peerId, data[0].id))
            }
            val ids: MutableList<Pair<Long, Int>> = ArrayList(data.size)
            for (msg in data) {
                ids.add(Pair(msg.peerId, msg.id))
            }
            return ids
        }

    override fun toString(): String {
        return "[$id] -> $data"
    }

    class Msg internal constructor(val id: Int, val peerId: Long): IdentificablePair {
        var isAlreadyExists = false
            private set
        var message: Message? = null
            private set
        var dto: VKApiMessage? = null
            private set
        private var backup: VKApiMessage? = null

        fun setDto(dto: VKApiMessage): Msg {
            this.dto = dto
            if (backup?.keyboard != null) {
                dto.keyboard = backup?.keyboard
                dto.payload = backup?.payload
            }
            return this
        }

        fun setMessage(message: Message?): Msg {
            this.message = message
            return this
        }

        fun setAlreadyExists(alreadyExists: Boolean): Msg {
            isAlreadyExists = alreadyExists
            return this
        }

        fun setBackup(backup: VKApiMessage?): Msg {
            this.backup = backup
            return this
        }

        override fun getObjectOwnerId(): Long {
            return peerId
        }

        override fun getObjectId(): Int {
            return id
        }
    }
}
