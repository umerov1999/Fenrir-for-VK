package dev.ragnarok.fenrir.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.interfaces.IdentificableOwner
import dev.ragnarok.fenrir.getBoolean
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.putBoolean
import dev.ragnarok.fenrir.readTypedObjectCompat
import dev.ragnarok.fenrir.settings.Settings.get
import dev.ragnarok.fenrir.util.Utils.firstNonEmptyString
import dev.ragnarok.fenrir.writeTypedObjectCompat

class Dialog : IdentificableOwner, Parcelable {
    var peerId = 0L
        private set
    private var title: String? = null
    var unreadCount = 0
        private set
    var photo50: String? = null
        private set
    var photo100: String? = null
        private set
    var photo200: String? = null
        private set
    var message: Message? = null
        private set
    var interlocutor: Owner? = null
        private set
    var lastCmId = 0
        private set
    var inReadCmId = 0
        private set
    var outReadCmId = 0
        private set
    var isGroupChannel = false
        private set
    var majorId = 0
        private set

    constructor()
    internal constructor(parcel: Parcel) {
        peerId = parcel.readLong()
        title = parcel.readString()
        unreadCount = parcel.readInt()
        photo50 = parcel.readString()
        photo100 = parcel.readString()
        photo200 = parcel.readString()
        message = parcel.readTypedObjectCompat(Message.CREATOR)
        interlocutor = ParcelableOwnerWrapper.readOwner(parcel)
        lastCmId = parcel.readInt()
        inReadCmId = parcel.readInt()
        outReadCmId = parcel.readInt()
        majorId = parcel.readInt()
        isGroupChannel = parcel.getBoolean()
    }

    override fun equals(other: Any?): Boolean {
        return other is Dialog && peerId == other.peerId
    }

    override fun hashCode(): Int {
        return peerId.hashCode()
    }

    fun setPeerId(peerId: Long): Dialog {
        this.peerId = peerId
        return this
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?): Dialog {
        this.title = title
        return this
    }

    fun setUnreadCount(unreadCount: Int): Dialog {
        this.unreadCount = unreadCount
        return this
    }

    fun setPhoto50(photo50: String?): Dialog {
        this.photo50 = photo50
        return this
    }

    fun setPhoto100(photo100: String?): Dialog {
        this.photo100 = photo100
        return this
    }

    fun setPhoto200(photo200: String?): Dialog {
        this.photo200 = photo200
        return this
    }

    val isChat: Boolean
        get() = Peer.isGroupChat(peerId)
    val isUser: Boolean
        get() = Peer.isUser(peerId)
    val isGroup: Boolean
        get() = Peer.isGroup(peerId)

    val isContact: Boolean
        get() = Peer.isContactChat(peerId)

    fun setMessage(message: Message?): Dialog {
        this.message = message
        return this
    }

    fun setInterlocutor(interlocutor: Owner?): Dialog {
        this.interlocutor = interlocutor
        return this
    }

    fun setLastCmId(lastCmId: Int): Dialog {
        this.lastCmId = lastCmId
        return this
    }

    val isLastMessageOut: Boolean
        get() = message?.isOut == true

    @ChatAction
    val lastMessageAction: Int
        get() = message?.action ?: ChatAction.NO_ACTION

    fun hasForwardMessages(): Boolean {
        return forwardMessagesCount > 0
    }

    val forwardMessagesCount: Int
        get() = message?.fwd?.size.orZero()

    fun hasAttachments(): Boolean {
        return message?.attachments?.hasAttachments == true
    }

    val lastMessageText: String?
        get() = if (message == null) "..." else message?.text

    fun getSenderShortName(context: Context): String {
        var targerText: String? = null
        if (interlocutor is User) {
            targerText = (interlocutor as User).firstName
        } else if (interlocutor is Community) {
            targerText = (interlocutor as Community).fullName
        }
        return targerText ?: context.getString(R.string.unknown_first_name)
    }

    fun getDisplayTitle(context: Context): String? {
        return when (Peer.getType(peerId)) {
            PeerType.USER, PeerType.GROUP -> {
                val custom = get().main().getUserNameChanges(peerId)
                if (custom.nonNullNoEmpty()) {
                    return custom
                }
                if (interlocutor == null && title.isNullOrEmpty()) context.getString(R.string.unknown_first_name) + " " + context.getString(
                    R.string.unknown_last_name
                )
                else if (title.nonNullNoEmpty()) {
                    title
                } else interlocutor?.fullName
            }

            PeerType.CHAT, PeerType.CONTACT -> title
            else -> throw IllegalStateException("Unknown peer id: $peerId")
        }
    }

    fun getDisplayTitle(): String? {
        return when (Peer.getType(peerId)) {
            PeerType.USER, PeerType.GROUP -> {
                val custom = get().main().getUserNameChanges(peerId)
                if (custom.nonNullNoEmpty()) {
                    return custom
                }
                if (interlocutor == null && title.isNullOrEmpty()) null
                else if (title.nonNullNoEmpty()) {
                    title
                } else interlocutor?.fullName
            }

            PeerType.CHAT, PeerType.CONTACT -> title
            else -> throw IllegalStateException("Unknown peer id: $peerId")
        }
    }

    val lastMessageDate: Long
        get() = message?.date.orZero()

    //if (isEmpty(img) && interlocutor != null) {
    //img = interlocutor.getMaxSquareAvatar();
    // }
    val imageUrl: String?
        get() {
            if (Peer.getType(peerId) == PeerType.CHAT || Peer.getType(peerId) == PeerType.CONTACT) {

                //if (isEmpty(img) && interlocutor != null) {
                //img = interlocutor.getMaxSquareAvatar();
                // }
                return firstNonEmptyString(photo200, photo100, photo50)
            }
            return interlocutor?.maxSquareAvatar
        }

    fun setInReadCmId(inReadCmId: Int): Dialog {
        this.inReadCmId = inReadCmId
        return this
    }

    val isLastMessageRead: Boolean
        get() = if (isLastMessageOut) lastCmId <= outReadCmId else lastCmId <= inReadCmId

    fun setOutReadCmId(outReadCmId: Int): Dialog {
        this.outReadCmId = outReadCmId
        return this
    }

    fun setGroupChannel(groupChannel: Boolean): Dialog {
        isGroupChannel = groupChannel
        return this
    }

    fun setMajorId(majorId: Int): Dialog {
        this.majorId = majorId
        return this
    }

    override fun getOwnerObjectId(): Long {
        return peerId
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(peerId)
        dest.writeString(title)
        dest.writeInt(unreadCount)
        dest.writeString(photo50)
        dest.writeString(photo100)
        dest.writeString(photo200)
        dest.writeTypedObjectCompat(message, flags)
        ParcelableOwnerWrapper.writeOwner(dest, flags, interlocutor)
        dest.writeInt(lastCmId)
        dest.writeInt(inReadCmId)
        dest.writeInt(outReadCmId)
        dest.writeInt(majorId)
        dest.putBoolean(isGroupChannel)
    }

    companion object CREATOR : Parcelable.Creator<Dialog> {
        override fun createFromParcel(parcel: Parcel): Dialog {
            return Dialog(parcel)
        }

        override fun newArray(size: Int): Array<Dialog?> {
            return arrayOfNulls(size)
        }
    }
}