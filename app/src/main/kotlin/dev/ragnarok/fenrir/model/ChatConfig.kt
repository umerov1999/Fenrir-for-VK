package dev.ragnarok.fenrir.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class ChatConfig : Parcelable {
    private var models: ModelsBundle
    private var closeOnSend = false
    private var initialText: String? = null
    private var uploadFiles: ArrayList<Uri>? = null
    private var uploadFilesMimeType: String? = null

    constructor() {
        models = ModelsBundle()
    }

    private constructor(`in`: Parcel) {
        closeOnSend = `in`.readByte().toInt() != 0
        models = `in`.readParcelable(ModelsBundle::class.java.classLoader)!!
        initialText = `in`.readString()
        uploadFiles = `in`.createTypedArrayList(Uri.CREATOR)
        uploadFilesMimeType = `in`.readString()
    }

    fun getModels(): ModelsBundle {
        return models
    }

    fun setModels(models: ModelsBundle) {
        this.models = models
    }

    fun isCloseOnSend(): Boolean {
        return closeOnSend
    }

    fun setCloseOnSend(closeOnSend: Boolean) {
        this.closeOnSend = closeOnSend
    }

    fun getInitialText(): String? {
        return initialText
    }

    fun setInitialText(initialText: String?) {
        this.initialText = initialText
    }

    fun getUploadFiles(): ArrayList<Uri>? {
        return uploadFiles
    }

    fun setUploadFiles(uploadFiles: ArrayList<Uri>?) {
        this.uploadFiles = uploadFiles
    }

    fun getUploadFilesMimeType(): String? {
        return uploadFilesMimeType
    }

    fun setUploadFilesMimeType(uploadFilesMimeType: String?) {
        this.uploadFilesMimeType = uploadFilesMimeType
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (closeOnSend) 1 else 0).toByte())
        dest.writeParcelable(models, flags)
        dest.writeString(initialText)
        dest.writeTypedList(uploadFiles)
        dest.writeString(uploadFilesMimeType)
    }

    fun appendAll(models: Iterable<AbsModel>) {
        for (model in models) {
            this.models.append(model)
        }
    }

    companion object CREATOR : Parcelable.Creator<ChatConfig> {
        override fun createFromParcel(parcel: Parcel): ChatConfig {
            return ChatConfig(parcel)
        }

        override fun newArray(size: Int): Array<ChatConfig?> {
            return arrayOfNulls(size)
        }
    }
}