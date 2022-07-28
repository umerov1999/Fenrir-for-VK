package dev.ragnarok.fenrir.util

import android.os.Parcel
import android.os.Parcelable

class BooleanValue : Parcelable {
    private var value: Boolean

    @JvmOverloads
    constructor(initialValue: Boolean = false) {
        value = initialValue
    }

    internal constructor(p: Parcel) {
        value = p.readByte().toInt() != 0
    }

    /**
     * @param value new boolean value
     * @return true if value was changed
     */
    fun setValue(value: Boolean): Boolean {
        if (this.value == value) {
            return false
        }
        this.value = value
        return true
    }

    fun get(): Boolean {
        return value
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (value) 1 else 0).toByte())
    }

    companion object CREATOR : Parcelable.Creator<BooleanValue> {
        override fun createFromParcel(parcel: Parcel): BooleanValue {
            return BooleanValue(parcel)
        }

        override fun newArray(size: Int): Array<BooleanValue?> {
            return arrayOfNulls(size)
        }
    }
}