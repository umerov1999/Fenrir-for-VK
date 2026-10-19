package dev.ragnarok.fenrir.db.model.entity

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
class DialogDboEntity(val peerId: Long) : DboEntity {
    var title: String? = null
        private set
    var unreadCount = 0
        private set
    var photo50: String? = null
        private set
    var photo100: String? = null
        private set
    var photo200: String? = null
        private set
    var inReadCmId = 0
        private set
    var outReadCmId = 0
        private set
    var message: MessageDboEntity? = null
        private set
    var currentKeyboard: KeyboardEntity? = null
        private set
    var pinned: MessageDboEntity? = null
        private set
    var lastCmId = 0
        private set
    var acl = 0
        private set
    var isGroupChannel = false
        private set
    var majorId = 0
        private set

    override fun getAttachmentKey(): String {
        return "$peerId"
    }

    fun setAcl(acl: Int): DialogDboEntity {
        this.acl = acl
        return this
    }

    fun setPinned(pinned: MessageDboEntity?): DialogDboEntity {
        this.pinned = pinned
        return this
    }

    fun setInReadCmId(inReadCmId: Int): DialogDboEntity {
        this.inReadCmId = inReadCmId
        return this
    }

    fun setOutReadCmId(outReadCmId: Int): DialogDboEntity {
        this.outReadCmId = outReadCmId
        return this
    }

    fun setTitle(title: String?): DialogDboEntity {
        this.title = title
        return this
    }

    fun setCurrentKeyboard(currentKeyboard: KeyboardEntity?): DialogDboEntity {
        this.currentKeyboard = currentKeyboard
        return this
    }

    fun setUnreadCount(unreadCount: Int): DialogDboEntity {
        this.unreadCount = unreadCount
        return this
    }

    fun setPhoto50(photo50: String?): DialogDboEntity {
        this.photo50 = photo50
        return this
    }

    fun toPeerDialog(): PeerDialogEntity {
        return PeerDialogEntity(peerId)
            .setTitle(title)
            .setPhoto200(photo200)
            .setPhoto100(photo100)
            .setPhoto50(photo50)
            .setOutReadCmId(outReadCmId)
            .setInReadCmId(inReadCmId)
            .setUnreadCount(unreadCount)
            .setPinned(pinned)
            .setLastCmId(lastCmId)
            .setAcl(acl)
            .setCurrentKeyboard(currentKeyboard)
            .setGroupChannel(isGroupChannel)
            .setMajorId(majorId)
    }

    fun setPhoto100(photo100: String?): DialogDboEntity {
        this.photo100 = photo100
        return this
    }

    fun setPhoto200(photo200: String?): DialogDboEntity {
        this.photo200 = photo200
        return this
    }

    fun setMessage(message: MessageDboEntity?): DialogDboEntity {
        this.message = message
        return this
    }

    fun setLastCmId(lastCmId: Int): DialogDboEntity {
        this.lastCmId = lastCmId
        return this
    }

    fun setGroupChannel(groupChannel: Boolean): DialogDboEntity {
        isGroupChannel = groupChannel
        return this
    }

    fun setMajorId(majorId: Int): DialogDboEntity {
        this.majorId = majorId
        return this
    }

}