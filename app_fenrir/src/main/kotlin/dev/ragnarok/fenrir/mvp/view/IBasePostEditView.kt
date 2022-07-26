package dev.ragnarok.fenrir.mvp.view

interface IBasePostEditView : IBaseAttachmentsEditView, IProgressView {
    fun displaySignerInfo(fullName: String?, photo: String?)
    fun setShowAuthorChecked(checked: Boolean)
    fun setSignerInfoVisible(visible: Boolean)
    fun setAddSignatureOptionVisible(visible: Boolean)
    fun setFromGroupOptionVisible(visible: Boolean)
    fun setFriendsOnlyOptionVisible(visible: Boolean)
    fun setFromGroupChecked(checked: Boolean)
    fun setFriendsOnlyChecked(checked: Boolean)
}