package dev.ragnarok.fenrir.mvp.view

import android.net.Uri
import dev.ragnarok.fenrir.model.AttachmentEntry
import dev.ragnarok.fenrir.model.LocalPhoto
import dev.ragnarok.fenrir.model.ModelsBundle
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IMessageAttachmentsView : IMvpView, IErrorView {
    fun displayAttachments(entries: List<AttachmentEntry>)
    fun notifyDataAdded(positionStart: Int, count: Int)
    fun addPhoto(accountId: Int, ownerId: Int)
    fun notifyEntryRemoved(index: Int)
    fun displaySelectUploadPhotoSizeDialog(photos: List<LocalPhoto>)
    fun displayCropPhotoDialog(uri: Uri)
    fun displaySelectUploadFileSizeDialog(file: String)
    fun changePercentageSmoothly(id: Int, progress: Int)
    fun notifyItemChanged(index: Int)
    fun setEmptyViewVisible(visible: Boolean)
    fun requestCameraPermission()
    fun startCamera(fileUri: Uri)
    fun syncAccompanyingWithParent(accompanying: ModelsBundle)
    fun startAddDocumentActivity(accountId: Int)
    fun startAddVideoActivity(accountId: Int, ownerId: Int)
    fun startAddAudioActivity(accountId: Int)
}