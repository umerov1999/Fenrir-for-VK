package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Comment

interface ICommentEditView : IBaseAttachmentsEditView, IProgressView {
    fun goBackWithResult(comment: Comment)
    fun showConfirmWithoutSavingDialog()
    fun goBack()
}