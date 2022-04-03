package dev.ragnarok.fenrir.fragment.search.criteria

import android.os.Parcel
import android.os.Parcelable

class DialogsSearchCriteria : BaseSearchCriteria {
    constructor(query: String?) : super(query)
    private constructor(`in`: Parcel) : super(`in`)

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DialogsSearchCriteria> =
            object : Parcelable.Creator<DialogsSearchCriteria> {
                override fun createFromParcel(`in`: Parcel): DialogsSearchCriteria {
                    return DialogsSearchCriteria(`in`)
                }

                override fun newArray(size: Int): Array<DialogsSearchCriteria?> {
                    return arrayOfNulls(size)
                }
            }
    }
}