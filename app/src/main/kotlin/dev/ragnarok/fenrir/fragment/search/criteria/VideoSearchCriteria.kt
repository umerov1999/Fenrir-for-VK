package dev.ragnarok.fenrir.fragment.search.criteria

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.search.options.SimpleBooleanOption
import dev.ragnarok.fenrir.fragment.search.options.SimpleNumberOption
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption

class VideoSearchCriteria : BaseSearchCriteria {
    constructor(query: String?, in_main_page: Boolean) : super(query) {
        val sort = SpinnerOption(KEY_SORT, R.string.sorting, true)
        sort.available = ArrayList(3)
        sort.available.add(SpinnerOption.Entry(0, R.string.by_date_added))
        sort.available.add(SpinnerOption.Entry(1, R.string.by_relevance))
        sort.available.add(SpinnerOption.Entry(2, R.string.by_duration))
        appendOption(sort)
        appendOption(SimpleBooleanOption(KEY_HD, R.string.hd_only, true))
        appendOption(
            SimpleBooleanOption(
                KEY_ADULT, R.string.disable_safe_search,
                active = true,
                checked = true
            )
        )
        appendOption(SimpleBooleanOption(KEY_YOUTUBE, R.string.youtube, true))
        appendOption(SimpleBooleanOption(KEY_VIMEO, R.string.vimeo, true))
        appendOption(SimpleBooleanOption(KEY_SHORT, R.string.short_videos, true))
        appendOption(SimpleBooleanOption(KEY_LONG, R.string.long_videos, true))
        appendOption(
            SimpleBooleanOption(
                KEY_SEARCH_OWN,
                R.string.search_in_my_videos,
                true,
                in_main_page
            )
        )
        appendOption(SimpleNumberOption(KEY_DURATION_FROM, R.string.min_duration, true))
        appendOption(SimpleNumberOption(KEY_DURATION_TO, R.string.max_duration, true))
    }

    private constructor(`in`: Parcel) : super(`in`)

    override fun describeContents(): Int {
        return 0
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): VideoSearchCriteria {
        return super.clone() as VideoSearchCriteria
    }

    companion object {
        const val KEY_SORT = 1
        const val KEY_HD = 2
        const val KEY_ADULT = 3
        const val KEY_YOUTUBE = 4
        const val KEY_VIMEO = 5
        const val KEY_SHORT = 6
        const val KEY_LONG = 7
        const val KEY_SEARCH_OWN = 8
        const val KEY_DURATION_FROM = 9
        const val KEY_DURATION_TO = 10

        @JvmField
        val CREATOR: Parcelable.Creator<VideoSearchCriteria> =
            object : Parcelable.Creator<VideoSearchCriteria> {
                override fun createFromParcel(`in`: Parcel): VideoSearchCriteria {
                    return VideoSearchCriteria(`in`)
                }

                override fun newArray(size: Int): Array<VideoSearchCriteria?> {
                    return arrayOfNulls(size)
                }
            }
    }
}