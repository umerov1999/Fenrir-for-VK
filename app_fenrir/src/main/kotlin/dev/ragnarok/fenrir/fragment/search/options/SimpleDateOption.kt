package dev.ragnarok.fenrir.fragment.search.options

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.getBoolean
import dev.ragnarok.fenrir.putBoolean

class SimpleDateOption : BaseOption {
    var timeUnix = 0L
    var onlyDate = false

    constructor(key: Int, title: Int, active: Boolean, onlyDate: Boolean) : super(
        DATE_TIME,
        key,
        title,
        active
    ) {
        this.onlyDate = onlyDate
    }

    internal constructor(parcel: Parcel) : super(parcel) {
        timeUnix = parcel.readLong()
        onlyDate = parcel.getBoolean()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeLong(timeUnix)
        dest.putBoolean(onlyDate)
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is SimpleDateOption && timeUnix == other.timeUnix && onlyDate == other.onlyDate
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + timeUnix.hashCode()
        result = 31 * result + if (onlyDate) 1 else 0
        return result
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): SimpleDateOption {
        return super.clone() as SimpleDateOption
    }

    companion object CREATOR : Parcelable.Creator<SimpleDateOption> {
        override fun createFromParcel(parcel: Parcel): SimpleDateOption {
            return SimpleDateOption(parcel)
        }

        override fun newArray(size: Int): Array<SimpleDateOption?> {
            return arrayOfNulls(size)
        }
    }
}