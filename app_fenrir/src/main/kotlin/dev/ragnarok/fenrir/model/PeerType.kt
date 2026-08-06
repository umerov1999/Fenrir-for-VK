package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef

@IntDef(PeerType.USER, PeerType.GROUP, PeerType.CHAT, PeerType.CONTACT)
@Retention(AnnotationRetention.SOURCE)
annotation class PeerType {
    companion object {
        const val USER = 0
        const val GROUP = 1
        const val CHAT = 2
        const val CONTACT = 3
    }
}
