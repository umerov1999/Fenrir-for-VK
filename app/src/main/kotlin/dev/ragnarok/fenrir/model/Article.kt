package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable

class Article : AbsModel {
    val id: Int
    val ownerId: Int
    var ownerName: String? = null
        private set
    var uRL: String? = null
        private set
    var title: String? = null
        private set
    var subTitle: String? = null
        private set
    var photo: Photo? = null
        private set
    var accessKey: String? = null
        private set
    var isFavorite = false
        private set

    constructor(id: Int, owner_id: Int) {
        this.id = id
        ownerId = owner_id
    }

    private constructor(`in`: Parcel) : super(`in`) {
        id = `in`.readInt()
        ownerId = `in`.readInt()
        ownerName = `in`.readString()
        uRL = `in`.readString()
        title = `in`.readString()
        subTitle = `in`.readString()
        accessKey = `in`.readString()
        photo = `in`.readParcelable(Photo::class.java.classLoader)
        isFavorite = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        super.writeToParcel(parcel, i)
        parcel.writeInt(id)
        parcel.writeInt(ownerId)
        parcel.writeString(ownerName)
        parcel.writeString(uRL)
        parcel.writeString(title)
        parcel.writeString(subTitle)
        parcel.writeString(accessKey)
        parcel.writeParcelable(photo, i)
        parcel.writeByte((if (isFavorite) 1 else 0).toByte())
    }

    fun setOwnerName(owner_name: String?): Article {
        ownerName = owner_name
        return this
    }

    fun setURL(url: String?): Article {
        uRL = url
        return this
    }

    fun setTitle(title: String?): Article {
        this.title = title
        return this
    }

    fun setSubTitle(subtitle: String?): Article {
        subTitle = subtitle
        return this
    }

    fun setPhoto(photo: Photo?): Article {
        this.photo = photo
        return this
    }

    fun setAccessKey(access_key: String?): Article {
        accessKey = access_key
        return this
    }

    fun setIsFavorite(is_favorite: Boolean): Article {
        isFavorite = is_favorite
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Article) return false
        return id == other.id && ownerId == other.ownerId
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + ownerId
        return result
    }

    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }
}