package dev.ragnarok.fenrir.fragment.search.criteria

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.search.options.SimpleDateOption
import dev.ragnarok.fenrir.readObjectLong
import dev.ragnarok.fenrir.writeObjectLong

class MessageSearchCriteria : BaseSearchCriteria {
    var peerId: Long? = null
        private set

    constructor(query: String?) : super(query) {
        appendOption(
            SimpleDateOption(
                key = KEY_END_TIME,
                title = R.string.date_to,
                active = true,
                onlyDate = true
            )
        )
    }

    internal constructor(parcel: Parcel) : super(parcel) {
        peerId = parcel.readObjectLong()
    }

    fun setPeerId(peerId: Long?): MessageSearchCriteria {
        this.peerId = peerId
        return this
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeObjectLong(peerId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val KEY_END_TIME = 1

        @JvmField
        val CREATOR: Parcelable.Creator<MessageSearchCriteria> =
            object : Parcelable.Creator<MessageSearchCriteria> {
                override fun createFromParcel(parcel: Parcel): MessageSearchCriteria {
                    return MessageSearchCriteria(parcel)
                }

                override fun newArray(size: Int): Array<MessageSearchCriteria?> {
                    return arrayOfNulls(size)
                }
            }
    }
}