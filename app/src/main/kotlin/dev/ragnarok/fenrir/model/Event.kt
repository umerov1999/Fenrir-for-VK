package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable

class Event : AbsModel {
    val id: Int
    var button_text: String? = null
        private set
    var text: String? = null
        private set
    var subject: Owner? = null
        private set

    constructor(id: Int) {
        this.id = id
    }

    private constructor(`in`: Parcel) : super(`in`) {
        id = `in`.readInt()
        button_text = `in`.readString()
        text = `in`.readString()
        subject =
            `in`.readParcelable(if (id > 0) User::class.java.classLoader else Community::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        super.writeToParcel(parcel, i)
        parcel.writeInt(id)
        parcel.writeString(button_text)
        parcel.writeString(text)
        parcel.writeParcelable(subject, i)
    }

    fun setText(text: String?): Event {
        this.text = text
        return this
    }

    fun setButton_text(button_text: String?): Event {
        this.button_text = button_text
        return this
    }

    fun setSubject(subject: Owner?): Event {
        this.subject = subject
        return this
    }

    val subjectPhoto: String?
        get() = subject?.maxSquareAvatar
    val subjectName: String?
        get() = subject?.fullName

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}