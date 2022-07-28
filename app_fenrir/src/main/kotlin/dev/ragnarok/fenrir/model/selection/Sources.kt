package dev.ragnarok.fenrir.model.selection

import android.os.Parcel
import android.os.Parcelable

class Sources : Parcelable {
    val sources: ArrayList<AbsSelectableSource>

    constructor() {
        sources = ArrayList(2)
    }

    internal constructor(`in`: Parcel) {
        val size = `in`.readInt()
        sources = ArrayList(size)
        for (i in 0 until size) {
            when (@Types val type = `in`.readInt()) {
                Types.FILES -> sources.add(
                    `in`.readParcelable(
                        FileManagerSelectableSource::class.java.classLoader
                    )!!
                )
                Types.LOCAL_PHOTOS -> sources.add(
                    `in`.readParcelable(
                        LocalPhotosSelectableSource::class.java.classLoader
                    )!!
                )
                Types.LOCAL_GALLERY -> sources.add(
                    `in`.readParcelable(
                        LocalGallerySelectableSource::class.java.classLoader
                    )!!
                )
                Types.VIDEOS -> sources.add(
                    `in`.readParcelable(
                        LocalVideosSelectableSource::class.java.classLoader
                    )!!
                )
                Types.VK_PHOTOS -> sources.add(
                    `in`.readParcelable(
                        VkPhotosSelectableSource::class.java.classLoader
                    )!!
                )
                else -> throw UnsupportedOperationException("Invalid type $type")
            }
        }
    }

    fun with(source: AbsSelectableSource): Sources {
        sources.add(source)
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    fun count(): Int {
        return sources.size
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(sources.size)
        for (source in sources) {
            parcel.writeInt(source.type)
            parcel.writeParcelable(source, flags)
        }
    }

    operator fun get(position: Int): AbsSelectableSource {
        return sources[position]
    }

    companion object CREATOR : Parcelable.Creator<Sources> {
        override fun createFromParcel(parcel: Parcel): Sources {
            return Sources(parcel)
        }

        override fun newArray(size: Int): Array<Sources?> {
            return arrayOfNulls(size)
        }
    }
}