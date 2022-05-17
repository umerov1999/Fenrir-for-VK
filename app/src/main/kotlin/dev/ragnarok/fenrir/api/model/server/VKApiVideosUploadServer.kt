package dev.ragnarok.fenrir.api.model.server

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class VKApiVideosUploadServer : UploadServer, Parcelable {
    @SerializedName("upload_url")
    override var url: String? = null

    @Suppress("UNUSED")
    constructor()
    private constructor(`in`: Parcel) {
        url = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
    }

    companion object CREATOR : Parcelable.Creator<VKApiVideosUploadServer> {
        override fun createFromParcel(parcel: Parcel): VKApiVideosUploadServer {
            return VKApiVideosUploadServer(parcel)
        }

        override fun newArray(size: Int): Array<VKApiVideosUploadServer?> {
            return arrayOfNulls(size)
        }
    }
}