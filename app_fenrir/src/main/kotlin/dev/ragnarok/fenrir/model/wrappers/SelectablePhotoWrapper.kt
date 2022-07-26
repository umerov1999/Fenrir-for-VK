package dev.ragnarok.fenrir.model.wrappers

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.model.ISelectable
import dev.ragnarok.fenrir.model.Photo

class SelectablePhotoWrapper : Parcelable, Comparable<SelectablePhotoWrapper>, ISelectable {
    val photo: Photo
    override var isSelected = false
    var isDownloaded = false
        private set
    var index = 0
    var current = false

    constructor(photo: Photo) {
        this.photo = photo
    }

    private constructor(`in`: Parcel) {
        photo = `in`.readParcelable(Photo::class.java.classLoader)!!
        isSelected = `in`.readByte().toInt() != 0
        index = `in`.readInt()
        current = `in`.readByte().toInt() != 0
        isDownloaded = `in`.readByte().toInt() != 0
    }

    fun setDownloaded(downloaded: Boolean): SelectablePhotoWrapper {
        isDownloaded = downloaded
        return this
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(photo, flags)
        dest.writeByte((if (isSelected) 1 else 0).toByte())
        dest.writeInt(index)
        dest.writeByte((if (current) 1 else 0).toByte())
        dest.writeByte((if (isDownloaded) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SelectablePhotoWrapper
        return photo == that.photo
    }

    override fun hashCode(): Int {
        return photo.hashCode()
    }

    override fun compareTo(other: SelectablePhotoWrapper): Int {
        return index - other.index
    }

    companion object CREATOR : Parcelable.Creator<SelectablePhotoWrapper> {
        override fun createFromParcel(parcel: Parcel): SelectablePhotoWrapper {
            return SelectablePhotoWrapper(parcel)
        }

        override fun newArray(size: Int): Array<SelectablePhotoWrapper?> {
            return arrayOfNulls(size)
        }
    }
}