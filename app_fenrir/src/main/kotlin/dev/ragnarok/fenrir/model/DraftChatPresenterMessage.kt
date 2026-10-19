package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.readObjectInteger
import dev.ragnarok.fenrir.writeObjectInteger

class DraftChatPresenterMessage : Parcelable {
    var cmId: Int? = null
        private set
    var text: String? = null
        private set
    var dbAttachmentsCount: Int = 0
        private set

    constructor()
    internal constructor(parcel: Parcel) {
        cmId = parcel.readObjectInteger()
        text = parcel.readString()
        dbAttachmentsCount = parcel.readInt()
    }

    fun setCmId(cmId: Int?): DraftChatPresenterMessage {
        this.cmId = cmId
        return this
    }

    fun setText(text: String?): DraftChatPresenterMessage {
        this.text = text
        return this
    }

    fun setDbAttachmentsCount(count: Int): DraftChatPresenterMessage {
        this.dbAttachmentsCount = count
        return this
    }

    fun dbAttachmentsCountDec(): DraftChatPresenterMessage {
        dbAttachmentsCount--
        return this
    }

    fun dbAttachmentsCountInc(count: Int?): DraftChatPresenterMessage {
        if (count == null) {
            dbAttachmentsCount++
        } else {
            dbAttachmentsCount += count
        }
        return this
    }

    fun clear(): DraftChatPresenterMessage {
        cmId = null
        text = null
        dbAttachmentsCount = 0
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeObjectInteger(cmId)
        dest.writeString(text)
        dest.writeInt(dbAttachmentsCount)
    }

    companion object CREATOR : Parcelable.Creator<DraftChatPresenterMessage> {
        override fun createFromParcel(parcel: Parcel): DraftChatPresenterMessage {
            return DraftChatPresenterMessage(parcel)
        }

        override fun newArray(size: Int): Array<DraftChatPresenterMessage?> {
            return arrayOfNulls(size)
        }
    }
}