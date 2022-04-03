package dev.ragnarok.fenrir.mvp.view

interface IPostEditView : IBasePostEditView, IToastView, IToolbarView {
    fun closeAsSuccess()
    fun showConfirmExitDialog()
}