package dev.ragnarok.fenrir.fragment.search.nextfrom

import android.os.Parcel
import android.os.Parcelable

class StringNextFrom : AbsNextFrom {
    var nextFrom: String?

    constructor(nextFrom: String?) {
        this.nextFrom = nextFrom
    }

    constructor(`in`: Parcel) {
        nextFrom = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(nextFrom)
    }

    override fun reset() {
        nextFrom = null
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StringNextFrom> =
            object : Parcelable.Creator<StringNextFrom> {
                override fun createFromParcel(`in`: Parcel): StringNextFrom {
                    return StringNextFrom(`in`)
                }

                override fun newArray(size: Int): Array<StringNextFrom?> {
                    return arrayOfNulls(size)
                }
            }
    }
}