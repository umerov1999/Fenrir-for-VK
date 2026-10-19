package dev.ragnarok.fenrir.model

import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity

class PeerUpdate(val accountId: Long, val peerId: Long) {
    var readIn: Read? = null
    var readOut: Read? = null
    var lastMessage: LastMessage? = null
    var unread: Unread? = null
    var pin: Pin? = null
    var title: Title? = null
    var botKeyboard: BotKeyboard? = null

    class Read(val messageId: Int)
    class Unread(val count: Int)
    class LastMessage(val messageId: Int)
    class Pin(val pinned: Message?)
    class Title(val title: String?)
    class BotKeyboard(val keyboard: KeyboardEntity?)
}