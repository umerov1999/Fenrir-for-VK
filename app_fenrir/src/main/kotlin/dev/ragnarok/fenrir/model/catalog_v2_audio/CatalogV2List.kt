package dev.ragnarok.fenrir.model.catalog_v2_audio

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.api.model.catalog_v2_audio.VKApiCatalogV2ListResponse
import dev.ragnarok.fenrir.orZero

class CatalogV2List : Parcelable {
    var default_section: String? = null
        private set
    var sections: ArrayList<CatalogV2ListItem>? = null
        private set

    constructor(parcel: Parcel) {
        default_section = parcel.readString()
        sections = parcel.createTypedArrayList(CatalogV2ListItem.CREATOR)
    }

    constructor(object_api: VKApiCatalogV2ListResponse) {
        default_section = object_api.catalog?.default_section
        sections = ArrayList(object_api.catalog?.sections?.size.orZero())
        for (i in object_api.catalog?.sections.orEmpty()) {
            sections?.add(CatalogV2ListItem(i))
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(default_section)
        parcel.writeTypedList(sections)
    }

    override fun describeContents(): Int {
        return 0
    }

    class CatalogV2ListItem : Parcelable {
        var id: String? = null
            private set
        var title: String? = null
            private set
        var url: String? = null
            private set

        var customType: Int = TYPE_CATALOG
            private set

        constructor(parcel: Parcel) {
            id = parcel.readString()
            title = parcel.readString()
            url = parcel.readString()
        }

        constructor(type: Int, title: String) {
            customType = type
            this.title = title
        }

        constructor(object_api: VKApiCatalogV2ListResponse.CatalogV2Sections.CatalogV2Section) {
            id = object_api.id
            title = object_api.title
            url = object_api.url
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(url)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
            const val TYPE_CATALOG = 0
            const val TYPE_LOCAL_AUDIO = 1
            const val TYPE_LOCAL_SERVER_AUDIO = 2
            const val TYPE_AUDIO = 3
            const val TYPE_PLAYLIST = 4
            const val TYPE_RECOMMENDATIONS = 5

            @JvmField
            val CREATOR: Parcelable.Creator<CatalogV2ListItem> =
                object : Parcelable.Creator<CatalogV2ListItem> {
                    override fun createFromParcel(`in`: Parcel): CatalogV2ListItem {
                        return CatalogV2ListItem(`in`)
                    }

                    override fun newArray(size: Int): Array<CatalogV2ListItem?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }

    companion object CREATOR : Parcelable.Creator<CatalogV2List> {
        override fun createFromParcel(parcel: Parcel): CatalogV2List {
            return CatalogV2List(parcel)
        }

        override fun newArray(size: Int): Array<CatalogV2List?> {
            return arrayOfNulls(size)
        }
    }
}
