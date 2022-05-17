package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable

class PhotoAlbumEditor : Parcelable {
    private var title: String? = null
    private var description: String? = null
    private var privacyView: Privacy? = null
    private var privacyComment: Privacy? = null
    private var commentsDisabled = false
    private var uploadByAdminsOnly = false

    private constructor()
    private constructor(`in`: Parcel) {
        title = `in`.readString()
        description = `in`.readString()
        privacyView = `in`.readParcelable(Privacy::class.java.classLoader)
        privacyComment = `in`.readParcelable(Privacy::class.java.classLoader)
        commentsDisabled = `in`.readByte().toInt() != 0
        uploadByAdminsOnly = `in`.readByte().toInt() != 0
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?): PhotoAlbumEditor {
        this.title = title
        return this
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String?): PhotoAlbumEditor {
        this.description = description
        return this
    }

    fun getPrivacyView(): Privacy? {
        return privacyView
    }

    fun setPrivacyView(privacyView: Privacy?): PhotoAlbumEditor {
        this.privacyView = privacyView
        return this
    }

    fun getPrivacyComment(): Privacy? {
        return privacyComment
    }

    fun setPrivacyComment(privacyComment: Privacy?): PhotoAlbumEditor {
        this.privacyComment = privacyComment
        return this
    }

    fun isCommentsDisabled(): Boolean {
        return commentsDisabled
    }

    fun setCommentsDisabled(commentsDisabled: Boolean): PhotoAlbumEditor {
        this.commentsDisabled = commentsDisabled
        return this
    }

    fun isUploadByAdminsOnly(): Boolean {
        return uploadByAdminsOnly
    }

    fun setUploadByAdminsOnly(uploadByAdminsOnly: Boolean): PhotoAlbumEditor {
        this.uploadByAdminsOnly = uploadByAdminsOnly
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeParcelable(privacyView, i)
        parcel.writeParcelable(privacyComment, i)
        parcel.writeByte((if (commentsDisabled) 1 else 0).toByte())
        parcel.writeByte((if (uploadByAdminsOnly) 1 else 0).toByte())
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PhotoAlbumEditor> =
            object : Parcelable.Creator<PhotoAlbumEditor> {
                override fun createFromParcel(`in`: Parcel): PhotoAlbumEditor {
                    return PhotoAlbumEditor(`in`)
                }

                override fun newArray(size: Int): Array<PhotoAlbumEditor?> {
                    return arrayOfNulls(size)
                }
            }

        fun create(): PhotoAlbumEditor {
            val editor = PhotoAlbumEditor()
            editor.setPrivacyComment(Privacy())
            editor.setPrivacyView(Privacy())
            return editor
        }
    }
}