package dev.ragnarok.fenrir.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class LocalPhoto : Parcelable, Comparable<LocalPhoto>, ISelectable {
    private var imageId: Long = 0
    private var fullImageUri: Uri? = null
    private var selected = false
    private var index = 0

    constructor()
    private constructor(`in`: Parcel) {
        imageId = `in`.readLong()
        fullImageUri = Uri.parse(`in`.readString())
        selected = `in`.readInt() == 1
        index = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(imageId)
        if (fullImageUri == null) {
            dest.writeString("null")
        } else {
            dest.writeString(fullImageUri.toString())
        }
        dest.writeInt(if (selected) 1 else 0)
        dest.writeInt(index)
    }

    fun getImageId(): Long {
        return imageId
    }

    fun setImageId(imageId: Long): LocalPhoto {
        this.imageId = imageId
        return this
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int): LocalPhoto {
        this.index = index
        return this
    }

    fun getFullImageUri(): Uri? {
        return fullImageUri
    }

    fun setFullImageUri(fullImageUri: Uri?): LocalPhoto {
        this.fullImageUri = fullImageUri
        return this
    }

    override fun compareTo(other: LocalPhoto): Int {
        return index - other.index
    }

    override val isSelected: Boolean
        get() = selected

    fun setSelected(selected: Boolean): LocalPhoto {
        this.selected = selected
        return this
    }

    companion object CREATOR : Parcelable.Creator<LocalPhoto> {
        override fun createFromParcel(parcel: Parcel): LocalPhoto {
            return LocalPhoto(parcel)
        }

        override fun newArray(size: Int): Array<LocalPhoto?> {
            return arrayOfNulls(size)
        }
    }
}