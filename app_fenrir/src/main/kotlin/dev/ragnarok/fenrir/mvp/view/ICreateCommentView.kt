package dev.ragnarok.fenrir.mvp.view

interface ICreateCommentView : IBaseAttachmentsEditView {
    fun returnDataToParent(textBody: String?)
    fun goBack()
}