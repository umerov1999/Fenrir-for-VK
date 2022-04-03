package dev.ragnarok.fenrir.fragment.search.criteria

import android.os.Parcel
import android.os.Parcelable

class AudioPlaylistSearchCriteria : BaseSearchCriteria {
    constructor(query: String?) : super(query)
    private constructor(`in`: Parcel) : super(`in`)

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AudioPlaylistSearchCriteria> =
            object : Parcelable.Creator<AudioPlaylistSearchCriteria> {
                override fun createFromParcel(`in`: Parcel): AudioPlaylistSearchCriteria {
                    return AudioPlaylistSearchCriteria(`in`)
                }

                override fun newArray(size: Int): Array<AudioPlaylistSearchCriteria?> {
                    return arrayOfNulls(size)
                }
            }
    }
}