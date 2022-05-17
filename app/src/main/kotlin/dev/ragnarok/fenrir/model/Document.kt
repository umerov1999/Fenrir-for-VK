package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.nonNullNoEmpty

class Document : AbsModel {
    val id: Int
    val ownerId: Int
    var title: String? = null
        private set
    var size: Long = 0
        private set
    var ext: String? = null
        private set
    var url: String? = null
        private set
    var date: Long = 0
        private set

    @DocType
    var type = 0
        private set
    var accessKey: String? = null
        private set
    var photoPreview: PhotoSizes? = null
        private set
    var videoPreview: VideoPreview? = null
        private set
    var graffiti: Graffiti? = null
        private set
    var msgId = 0
        private set
    var msgPeerId = 0
        private set

    constructor(id: Int, ownerId: Int) {
        this.id = id
        this.ownerId = ownerId
    }

    private constructor(`in`: Parcel) : super(`in`) {
        id = `in`.readInt()
        ownerId = `in`.readInt()
        title = `in`.readString()
        size = `in`.readLong()
        ext = `in`.readString()
        url = `in`.readString()
        date = `in`.readLong()
        type = `in`.readInt()
        accessKey = `in`.readString()
        photoPreview = `in`.readParcelable(PhotoSizes::class.java.classLoader)
        videoPreview = `in`.readParcelable(VideoPreview::class.java.classLoader)
        graffiti = `in`.readParcelable(Graffiti::class.java.classLoader)
        msgId = `in`.readInt()
        msgPeerId = `in`.readInt()
    }

    fun getPreviewWithSize(@PhotoSize size: Int, excludeNonAspectRatio: Boolean): String? {
        return photoPreview?.getUrlForSize(
            size,
            excludeNonAspectRatio
        )
    }

    fun setPhotoPreview(photoPreview: PhotoSizes?): Document {
        this.photoPreview = photoPreview
        return this
    }

    fun setVideoPreview(videoPreview: VideoPreview?): Document {
        this.videoPreview = videoPreview
        return this
    }

    fun getMaxPreviewSize(excludeNonAspectRatio: Boolean): PhotoSizes.Size? {
        return photoPreview?.getMaxSize(excludeNonAspectRatio)
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        super.writeToParcel(parcel, i)
        parcel.writeInt(id)
        parcel.writeInt(ownerId)
        parcel.writeString(title)
        parcel.writeLong(size)
        parcel.writeString(ext)
        parcel.writeString(url)
        parcel.writeLong(date)
        parcel.writeInt(type)
        parcel.writeString(accessKey)
        parcel.writeParcelable(photoPreview, i)
        parcel.writeParcelable(videoPreview, i)
        parcel.writeParcelable(graffiti, i)
        parcel.writeInt(msgId)
        parcel.writeInt(msgPeerId)
    }

    fun setGraffiti(graffiti: Graffiti?): Document {
        this.graffiti = graffiti
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    fun setTitle(title: String?): Document {
        this.title = title
        return this
    }

    fun setSize(size: Long): Document {
        this.size = size
        return this
    }

    fun setExt(ext: String?): Document {
        this.ext = ext
        return this
    }

    fun setUrl(url: String?): Document {
        this.url = url
        return this
    }

    fun setDate(date: Long): Document {
        this.date = date
        return this
    }

    fun setType(type: Int): Document {
        this.type = type
        return this
    }

    fun setAccessKey(accessKey: String?): Document {
        this.accessKey = accessKey
        return this
    }

    fun generateWebLink(): String {
        return String.format("vk.com/doc%s_%s", ownerId, id)
    }

    val isGif: Boolean
        get() = "gif" == ext

    fun hasValidGifVideoLink(): Boolean {
        return videoPreview?.src.nonNullNoEmpty()
    }

    fun setMsgId(msgId: Int): Document {
        this.msgId = msgId
        return this
    }

    fun setMsgPeerId(msgPeerId: Int): Document {
        this.msgPeerId = msgPeerId
        return this
    }

    class Graffiti : AbsModel {
        var src: String? = null
            private set
        var width = 0
            private set
        var height = 0
            private set

        constructor()
        private constructor(`in`: Parcel) : super(`in`) {
            src = `in`.readString()
            width = `in`.readInt()
            height = `in`.readInt()
        }

        override fun writeToParcel(parcel: Parcel, i: Int) {
            super.writeToParcel(parcel, i)
            parcel.writeString(src)
            parcel.writeInt(width)
            parcel.writeInt(height)
        }

        fun setSrc(src: String?): Graffiti {
            this.src = src
            return this
        }

        fun setWidth(width: Int): Graffiti {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Graffiti {
            this.height = height
            return this
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Graffiti> {
            override fun createFromParcel(parcel: Parcel): Graffiti {
                return Graffiti(parcel)
            }

            override fun newArray(size: Int): Array<Graffiti?> {
                return arrayOfNulls(size)
            }
        }
    }

    class VideoPreview : AbsModel {
        var src: String? = null
            private set
        var width = 0
            private set
        var height = 0
            private set
        var fileSize: Long = 0
            private set

        constructor()
        private constructor(`in`: Parcel) : super(`in`) {
            src = `in`.readString()
            width = `in`.readInt()
            height = `in`.readInt()
            fileSize = `in`.readLong()
        }

        override fun writeToParcel(parcel: Parcel, i: Int) {
            super.writeToParcel(parcel, i)
            parcel.writeString(src)
            parcel.writeInt(width)
            parcel.writeInt(height)
            parcel.writeLong(fileSize)
        }

        fun setSrc(src: String?): VideoPreview {
            this.src = src
            return this
        }

        fun setFileSize(fileSize: Long): VideoPreview {
            this.fileSize = fileSize
            return this
        }

        fun setWidth(width: Int): VideoPreview {
            this.width = width
            return this
        }

        fun setHeight(height: Int): VideoPreview {
            this.height = height
            return this
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<VideoPreview> {
            override fun createFromParcel(parcel: Parcel): VideoPreview {
                return VideoPreview(parcel)
            }

            override fun newArray(size: Int): Array<VideoPreview?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object CREATOR : Parcelable.Creator<Document> {
        override fun createFromParcel(parcel: Parcel): Document {
            return Document(parcel)
        }

        override fun newArray(size: Int): Array<Document?> {
            return arrayOfNulls(size)
        }
    }
}