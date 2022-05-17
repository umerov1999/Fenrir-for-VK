package dev.ragnarok.fenrir.mvp.view

import android.net.Uri

interface IPostCreateView : IBasePostEditView, IToolbarView {
    fun goBack()
    fun displayUploadUriSizeDialog(uris: List<Uri>)
}