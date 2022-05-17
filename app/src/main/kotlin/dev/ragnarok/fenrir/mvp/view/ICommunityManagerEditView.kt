package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICommunityManagerEditView : IMvpView, IAccountDependencyView, IErrorView, IProgressView,
    IToastView {
    fun displayUserInfo(user: User)
    fun showUserProfile(accountId: Int, user: User)
    fun checkModerator()
    fun checkEditor()
    fun checkAdmin()
    fun setShowAsContactCheched(cheched: Boolean)
    fun setContactInfoVisible(visible: Boolean)
    fun displayPosition(position: String?)
    fun displayEmail(email: String?)
    fun displayPhone(phone: String?)
    fun configRadioButtons(isCreator: Boolean)
    fun goBack()
    fun setDeleteOptionVisible(visible: Boolean)
}