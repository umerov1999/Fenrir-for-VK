package dev.ragnarok.fenrir.fragment.search.options

import android.os.Parcel
import android.os.Parcelable

class SimpleBooleanOption : BaseOption {
    var checked = false

    constructor(key: Int, title: Int, active: Boolean) : super(
        SIMPLE_BOOLEAN,
        key,
        title,
        active
    )

    constructor(key: Int, title: Int, active: Boolean, checked: Boolean) : super(
        SIMPLE_BOOLEAN,
        key,
        title,
        active
    ) {
        this.checked = checked
    }

    private constructor(`in`: Parcel) : super(`in`) {
        checked = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeByte((if (checked) 1 else 0).toByte())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as SimpleBooleanOption
        return checked == that.checked
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + if (checked) 1 else 0
        return result
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): SimpleBooleanOption {
        return super.clone() as SimpleBooleanOption
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SimpleBooleanOption> {
        override fun createFromParcel(parcel: Parcel): SimpleBooleanOption {
            return SimpleBooleanOption(parcel)
        }

        override fun newArray(size: Int): Array<SimpleBooleanOption?> {
            return arrayOfNulls(size)
        }
    }
}