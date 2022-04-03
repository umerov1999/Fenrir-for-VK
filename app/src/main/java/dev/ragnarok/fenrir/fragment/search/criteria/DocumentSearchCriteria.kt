package dev.ragnarok.fenrir.fragment.search.criteria

import android.os.Parcel
import android.os.Parcelable

class DocumentSearchCriteria : BaseSearchCriteria {
    constructor(query: String?) : super(query)
    private constructor(`in`: Parcel) : super(`in`)

    override fun describeContents(): Int {
        return 0
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): DocumentSearchCriteria {
        return super.clone() as DocumentSearchCriteria
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DocumentSearchCriteria> =
            object : Parcelable.Creator<DocumentSearchCriteria> {
                override fun createFromParcel(`in`: Parcel): DocumentSearchCriteria {
                    return DocumentSearchCriteria(`in`)
                }

                override fun newArray(size: Int): Array<DocumentSearchCriteria?> {
                    return arrayOfNulls(size)
                }
            }
    }
}