package dev.ragnarok.fenrir.db.model

import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity
import dev.ragnarok.fenrir.db.model.entity.MessageDboEntity

class PeerPatch(val id: Long) {
    var inRead: ReadTo? = null
        private set
    var outRead: ReadTo? = null
        private set
    var unread: Unread? = null
        private set
    var lastMessage: LastMessage? = null
        private set
    var pin: Pin? = null
        private set
    var title: Title? = null
        private set
    var botKeyboard: BotKeyboard? = null
        private set
    var majorId: MajorId? = null
        private set
    var minorId: MinorId? = null
        private set

    fun withInReadCmId(cmId: Int): PeerPatch {
        inRead = ReadTo(cmId)
        return this
    }

    fun withOutReadCmId(cmId: Int): PeerPatch {
        outRead = ReadTo(cmId)
        return this
    }

    fun withUnreadCount(count: Int): PeerPatch {
        unread = Unread(count)
        return this
    }

    fun withLastMessage(cmId: Int, minorId: Int): PeerPatch {
        if (cmId != 0) {
            lastMessage = LastMessage(cmId, minorId)
        }
        return this
    }

    fun withPin(pinned: MessageDboEntity?): PeerPatch {
        pin = Pin(pinned)
        return this
    }

    fun withTitle(title: String?): PeerPatch {
        this.title = Title(title)
        return this
    }

    fun withKeyboard(keyboard: KeyboardEntity?): PeerPatch {
        botKeyboard = BotKeyboard(keyboard)
        return this
    }

    fun withMajorId(majorId: Int): PeerPatch {
        this.majorId = MajorId(majorId)
        return this
    }

    fun withMinorId(minorId: Int): PeerPatch {
        this.minorId = MinorId(minorId)
        return this
    }

    class Title internal constructor(val title: String?)
    class Unread internal constructor(val count: Int)
    class Pin internal constructor(val pinned: MessageDboEntity?)
    class LastMessage internal constructor(val cmId: Int, val minorId: Int)
    class ReadTo internal constructor(val cmId: Int)
    class BotKeyboard internal constructor(val keyboard: KeyboardEntity?)
    class MajorId internal constructor(val majorId: Int)
    class MinorId internal constructor(val minorId: Int)
}