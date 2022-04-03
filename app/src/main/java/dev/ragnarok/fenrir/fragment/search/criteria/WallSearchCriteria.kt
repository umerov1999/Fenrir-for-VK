package dev.ragnarok.fenrir.fragment.search.criteria

import android.os.Parcel
import android.os.Parcelable

class WallSearchCriteria : BaseSearchCriteria {
    val ownerId: Int

    constructor(query: String?, ownerId: Int) : super(query) {
        this.ownerId = ownerId
    }

    private constructor(`in`: Parcel) : super(`in`) {
        ownerId = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(ownerId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<WallSearchCriteria> =
            object : Parcelable.Creator<WallSearchCriteria> {
                override fun createFromParcel(`in`: Parcel): WallSearchCriteria {
                    return WallSearchCriteria(`in`)
                }

                override fun newArray(size: Int): Array<WallSearchCriteria?> {
                    return arrayOfNulls(size)
                }
            }
    }
}