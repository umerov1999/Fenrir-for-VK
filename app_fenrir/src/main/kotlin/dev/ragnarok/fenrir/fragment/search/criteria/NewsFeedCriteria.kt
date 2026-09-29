package dev.ragnarok.fenrir.fragment.search.criteria

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.search.options.SimpleDateOption
import dev.ragnarok.fenrir.fragment.search.options.SimpleGPSOption

class NewsFeedCriteria : BaseSearchCriteria {
    constructor(query: String?) : super(query) {
        appendOption(SimpleGPSOption(KEY_GPS, R.string.gps, true))
        appendOption(
            SimpleDateOption(
                key = KEY_START_TIME,
                title = R.string.date_start,
                active = true,
                onlyDate = false
            )
        )
        appendOption(
            SimpleDateOption(
                key = KEY_END_TIME,
                title = R.string.date_to,
                active = true,
                onlyDate = false
            )
        )
    }

    internal constructor(parcel: Parcel) : super(parcel)

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val KEY_GPS = 1
        const val KEY_START_TIME = 2
        const val KEY_END_TIME = 3

        @JvmField
        val CREATOR: Parcelable.Creator<NewsFeedCriteria> =
            object : Parcelable.Creator<NewsFeedCriteria> {
                override fun createFromParcel(parcel: Parcel): NewsFeedCriteria {
                    return NewsFeedCriteria(parcel)
                }

                override fun newArray(size: Int): Array<NewsFeedCriteria?> {
                    return arrayOfNulls(size)
                }
            }
    }
}