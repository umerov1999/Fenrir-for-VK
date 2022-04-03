package dev.ragnarok.fenrir.util

import android.os.Parcel
import android.os.Parcelable

class BooleanValue : Parcelable {
    private var value: Boolean

    @JvmOverloads
    constructor(initialValue: Boolean = false) {
        value = initialValue
    }

    private constructor(p: Parcel) {
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

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BooleanValue> = object : Parcelable.Creator<BooleanValue> {
            override fun createFromParcel(`in`: Parcel): BooleanValue {
                return BooleanValue(`in`)
            }

            override fun newArray(size: Int): Array<BooleanValue?> {
                return arrayOfNulls(size)
            }
        }
    }
}