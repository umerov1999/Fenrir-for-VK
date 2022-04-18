package dev.ragnarok.fenrir.api.model

import java.util.*

class VKApiAttachments {
    val entries: ArrayList<Entry>

    constructor() {
        entries = ArrayList(1)
    }

    constructor(initialSize: Int) {
        entries = ArrayList(initialSize)
    }

    fun entryList(): List<Entry> {
        return Collections.unmodifiableList(entries)
    }

    fun append(attachment: VKApiAttachment) {
        entries.add(Entry(attachment.type, attachment))
    }

    fun clear() {
        entries.clear()
    }

    fun append(data: List<VKApiAttachment>) {
        for (attachment in data) {
            append(attachment)
        }
    }

    val isEmpty: Boolean
        get() = size() == 0

    fun nonEmpty(): Boolean {
        return size() > 0
    }

    fun size(): Int {
        return entries.size
    }

    class Entry(val type: String, val attachment: VKApiAttachment)
}