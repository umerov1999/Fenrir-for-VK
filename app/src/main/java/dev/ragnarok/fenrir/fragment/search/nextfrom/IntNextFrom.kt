package dev.ragnarok.fenrir.fragment.search.nextfrom

import android.os.Parcel
import android.os.Parcelable

class IntNextFrom : AbsNextFrom {
    var offset: Int

    constructor(initValue: Int) {
        offset = initValue
    }

    constructor(`in`: Parcel) {
        offset = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(offset)
    }

    override fun reset() {
        offset = 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<IntNextFrom> = object : Parcelable.Creator<IntNextFrom> {
            override fun createFromParcel(`in`: Parcel): IntNextFrom {
                return IntNextFrom(`in`)
            }

            override fun newArray(size: Int): Array<IntNextFrom?> {
                return arrayOfNulls(size)
            }
        }
    }
}